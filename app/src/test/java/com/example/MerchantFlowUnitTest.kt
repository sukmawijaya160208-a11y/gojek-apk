package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppDatabase
import com.example.core.database.SeedData
import com.example.core.model.LedgerDirection
import com.example.core.model.MerchantOrderStatus
import com.example.core.repository.MerchantRepository
import com.example.core.repository.WalletRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MerchantFlowUnitTest {

  private lateinit var database: AppDatabase
  private lateinit var merchantRepository: MerchantRepository
  private lateinit var walletRepository: WalletRepository

  @Before
  fun setup() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    SeedData.populateDatabase(database)
    merchantRepository = MerchantRepository(database)
    walletRepository = WalletRepository(database)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testStoreOnboardingKycAndOpenStatusToggle() = runBlocking {
    val initialStore = merchantRepository.getStore("user_merch_01").first()
    assertNotNull("Store should exist after database seed", initialStore)
    assertEquals("VERIFIED", initialStore!!.verificationState)
    assertTrue("Store should be open initially", initialStore.isOpen)

    // Toggle store closed
    merchantRepository.updateStoreOpenStatus(initialStore.id, false)
    val closedStore = merchantRepository.getStore("user_merch_01").first()
    assertFalse("Store should now be closed", closedStore!!.isOpen)

    // Update KYC / legal business information
    merchantRepository.updateStoreKyc(
      storeId = initialStore.id,
      name = "Dapur Nusantara Resto Mega Kuningan",
      desc = "Cabang Mega Kuningan",
      address = "Jl. Prof. Dr. Satrio No. 11, Jakarta Selatan",
      phone = "+6281122334455",
      bankName = "Bank Mandiri",
      accountNum = "1230009876543",
      accountHolder = "PT Dapur Nusantara Kuliner",
      npwp = "01.234.567.8-012.000",
      nib = "NIB-9988776655"
    )

    val updatedStore = merchantRepository.getStore("user_merch_01").first()
    assertEquals("Dapur Nusantara Resto Mega Kuningan", updatedStore!!.name)
    assertEquals("Bank Mandiri", updatedStore.bankName)
    assertEquals("1230009876543", updatedStore.bankAccountNumber)
    assertEquals("NIB-9988776655", updatedStore.nibNumber)
  }

  @Test
  fun testCatalogCategoriesAndProductManagement() = runBlocking {
    val store = merchantRepository.getStore("user_merch_01").first()!!

    // Verify seeded categories
    val initialCategories = merchantRepository.getCategories(store.id).first()
    assertTrue("Initial categories should contain Makanan and Minuman", initialCategories.size >= 2)

    // Add new category
    merchantRepository.addCategory(store.id, "Menu Paket Hemat")
    val updatedCategories = merchantRepository.getCategories(store.id).first()
    assertTrue(updatedCategories.any { it.name == "Menu Paket Hemat" })

    // Add new product
    val targetCat = updatedCategories.first { it.name == "Menu Paket Hemat" }
    merchantRepository.addProduct(
      storeId = store.id,
      categoryId = targetCat.id,
      name = "Paket Nasi Timbel Komplit",
      description = "Nasi timbel bungkus daun pisang dengan ayam goreng, tahu tempe, lalap sambal.",
      price = 38000L,
      stockQuantity = 40
    )

    val products = merchantRepository.getProducts(store.id).first()
    val newProduct = products.find { it.name == "Paket Nasi Timbel Komplit" }
    assertNotNull("New product must be in catalog", newProduct)
    assertEquals(38000L, newProduct!!.price)
    assertEquals(40, newProduct.stockQuantity)
    assertTrue("Product must be available by default", newProduct.isAvailable)
  }

  @Test
  fun testStockInventoryAdjustmentAndAvailabilityToggle() = runBlocking {
    val store = merchantRepository.getStore("user_merch_01").first()!!
    val products = merchantRepository.getProducts(store.id).first()
    val product = products.first()

    // Update stock quantity
    merchantRepository.updateStock(product.id, 12)
    val updatedProduct = merchantRepository.getProducts(store.id).first().find { it.id == product.id }!!
    assertEquals(12, updatedProduct.stockQuantity)

    // Deplete stock to 0 (auto-toggles availability to false)
    merchantRepository.updateStock(product.id, 0)
    val depletedProduct = merchantRepository.getProducts(store.id).first().find { it.id == product.id }!!
    assertEquals(0, depletedProduct.stockQuantity)
    assertFalse("Out of stock product should not be available", depletedProduct.isAvailable)

    // Manual toggle back to available
    merchantRepository.toggleProductAvailability(product.id, true)
    val toggledProduct = merchantRepository.getProducts(store.id).first().find { it.id == product.id }!!
    assertTrue(toggledProduct.isAvailable)
  }

  @Test
  fun testIncomingOrderKitchenPreparationLifecycle() = runBlocking {
    val store = merchantRepository.getStore("user_merch_01").first()!!
    val activeOrders = merchantRepository.getActiveOrders(store.id).first()
    val targetOrder = activeOrders.first { it.status == MerchantOrderStatus.RECEIVED }

    // 1. Confirm and start cooking with 20 minutes ETA
    merchantRepository.startCooking(targetOrder.id, estimatedMinutes = 20)
    val preparingOrder = merchantRepository.getOrders(store.id).first().find { it.id == targetOrder.id }!!
    assertEquals(MerchantOrderStatus.PREPARING, preparingOrder.status)
    assertEquals(20, preparingOrder.estimatedPreparationMin)

    // 2. Mark ready for driver pickup
    merchantRepository.markReadyForPickup(targetOrder.id)
    val readyOrder = merchantRepository.getOrders(store.id).first().find { it.id == targetOrder.id }!!
    assertEquals(MerchantOrderStatus.READY_FOR_PICKUP, readyOrder.status)

    // 3. Driver picks up order
    merchantRepository.markPickedUpByDriver(targetOrder.id)
    val pickedUpOrder = merchantRepository.getOrders(store.id).first().find { it.id == targetOrder.id }!!
    assertEquals(MerchantOrderStatus.PICKED_UP, pickedUpOrder.status)

    // 4. Complete order and verify ledger settlement
    val balanceBefore = walletRepository.getCalculatedBalance("w_merch_01").first()
    merchantRepository.completeOrder(pickedUpOrder)

    val completedOrder = merchantRepository.getOrders(store.id).first().find { it.id == targetOrder.id }!!
    assertEquals(MerchantOrderStatus.COMPLETED, completedOrder.status)

    val balanceAfter = walletRepository.getCalculatedBalance("w_merch_01").first()
    assertEquals(balanceBefore + targetOrder.netMerchantAmount, balanceAfter)
  }

  @Test
  fun testMerchantSettlementPayoutRequest() = runBlocking {
    val initialBalance = walletRepository.getCalculatedBalance("w_merch_01").first()
    assertTrue("Merchant should have initial balance from seed/orders", initialBalance > 0)

    val payoutAmount = 100000L
    merchantRepository.requestSettlement(
      merchantId = "store_01",
      amount = payoutAmount,
      bankInfo = "BCA 0182938475"
    )

    val balanceAfter = walletRepository.getCalculatedBalance("w_merch_01").first()
    assertEquals(initialBalance - payoutAmount, balanceAfter)

    val ledger = walletRepository.getLedgerEntries("w_merch_01").first()
    val settlementEntry = ledger.find { it.direction == LedgerDirection.DEBIT && it.amount == payoutAmount }
    assertNotNull("Settlement debit entry must exist in ledger", settlementEntry)
    assertTrue(settlementEntry!!.description.contains("Pencairan Saldo"))
  }
}
