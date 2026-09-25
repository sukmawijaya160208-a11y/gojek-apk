package com.example.features.merchant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.MerchantCategoryEntity
import com.example.core.database.entity.MerchantOrderEntity
import com.example.core.database.entity.MerchantProductEntity
import com.example.core.database.entity.StoreEntity
import com.example.core.model.LedgerDirection
import com.example.core.model.MerchantOrderStatus
import com.example.core.repository.MerchantRepository
import com.example.core.repository.WalletRepository
import com.example.core.ui.components.PriceRow
import com.example.core.ui.components.SukmaButton
import com.example.core.ui.components.SukmaButtonVariant
import com.example.core.ui.components.SukmaCard
import com.example.core.ui.components.SukmaEmptyState
import com.example.features.customer.formatRupiah
import com.example.ui.theme.CardShape
import com.example.ui.theme.ControlShape
import com.example.ui.theme.PillShape
import com.example.ui.theme.SukmaEmergencyRed
import com.example.ui.theme.SukmaEmeraldPrimary
import com.example.ui.theme.SukmaMintAccent
import com.example.ui.theme.SukmaMintLight
import com.example.ui.theme.SukmaSlateDark
import com.example.ui.theme.SukmaSlateSubtle
import com.example.ui.theme.SukmaSuccessGreen
import com.example.ui.theme.SukmaSuccessLight
import com.example.ui.theme.SukmaSurgeAmber
import com.example.ui.theme.SukmaSurgeLight
import kotlinx.coroutines.launch

enum class MerchantTab(val label: String, val icon: ImageVector) {
  ORDERS("Pesanan", Icons.Default.Restaurant),
  CATALOG("Katalog", Icons.Default.RestaurantMenu),
  FINANCE("Pendapatan", Icons.Default.Payments),
  STORE_KYC("Profil Toko", Icons.Default.Store)
}

@Composable
fun MerchantHomeScreen(
  merchantRepository: MerchantRepository,
  walletRepository: WalletRepository
) {
  val scope = rememberCoroutineScope()
  var currentTab by remember { mutableStateOf(MerchantTab.ORDERS) }

  val store by merchantRepository.getStore("user_merch_01").collectAsState(
    initial = StoreEntity(
      id = "store_01",
      merchantUserId = "user_merch_01",
      name = "Dapur Nusantara Resto - Thamrin",
      description = "Spesialis masakan nusantara autentik, aneka nasi olahan, lauk bakar, dan minuman segar tradisional.",
      address = "Jl. M.H. Thamrin No. 28, Menteng, Jakarta Pusat",
      phone = "+6281399887766"
    )
  )

  val orders by merchantRepository.getOrders("store_01").collectAsState(initial = emptyList())
  val categories by merchantRepository.getCategories("store_01").collectAsState(initial = emptyList())
  val products by merchantRepository.getProducts("store_01").collectAsState(initial = emptyList())
  val walletBalance by walletRepository.getCalculatedBalance("w_merch_01").collectAsState(initial = 450000L)
  val ledgerEntries by walletRepository.getLedgerEntries("w_merch_01").collectAsState(initial = emptyList())

  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
      ) {
        MerchantTab.values().forEach { tab ->
          val isSelected = currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = tab },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.label
              )
            },
            label = {
              Text(
                text = tab.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = SukmaEmeraldPrimary,
              selectedTextColor = SukmaEmeraldPrimary,
              indicatorColor = SukmaEmeraldPrimary.copy(alpha = 0.15f),
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      val currentStore = store ?: StoreEntity(
        id = "store_01",
        merchantUserId = "user_merch_01",
        name = "Dapur Nusantara Resto",
        description = "Restoran Kuliner Nusantara",
        address = "Jl. Thamrin, Jakarta Pusat",
        phone = "+6281399887766"
      )

      when (currentTab) {
        MerchantTab.ORDERS -> {
          MerchantOrdersTab(
            store = currentStore,
            orders = orders,
            merchantRepository = merchantRepository
          )
        }
        MerchantTab.CATALOG -> {
          MerchantCatalogTab(
            store = currentStore,
            categories = categories,
            products = products,
            merchantRepository = merchantRepository
          )
        }
        MerchantTab.FINANCE -> {
          MerchantFinanceTab(
            store = currentStore,
            balance = walletBalance,
            ledgerEntries = ledgerEntries,
            merchantRepository = merchantRepository
          )
        }
        MerchantTab.STORE_KYC -> {
          MerchantStoreKycTab(
            store = currentStore,
            merchantRepository = merchantRepository
          )
        }
      }
    }
  }
}

/**
 * Tab 1: Merchant Incoming Orders & Kitchen Preparation Status
 */
@Composable
fun MerchantOrdersTab(
  store: StoreEntity,
  orders: List<MerchantOrderEntity>,
  merchantRepository: MerchantRepository
) {
  val scope = rememberCoroutineScope()
  val activeOrders = orders.filter { it.status != MerchantOrderStatus.COMPLETED && it.status != MerchantOrderStatus.CANCELLED }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Store Status Header Switch
      Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(
          containerColor = if (store.isOpen) SukmaEmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
          1.dp,
          if (store.isOpen) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(if (store.isOpen) SukmaEmeraldPrimary else Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = store.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (store.isOpen) "Resto BUKA • Siap menerima order" else "Resto TUTUP Sementara",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Switch(
            checked = store.isOpen,
            onCheckedChange = { isOpen ->
              scope.launch {
                merchantRepository.updateStoreOpenStatus(store.id, isOpen)
              }
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = SukmaEmeraldPrimary
            )
          )
        }
      }
    }

    item {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Antrean Dapur (${activeOrders.size} Aktif)",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    if (activeOrders.isEmpty()) {
      item {
        SukmaEmptyState(
          title = "Belum Ada Pesanan Masuk",
          description = "Pesanan makanan baru dari pelanggan Go Sukma Food akan muncul secara langsung di sini.",
          icon = Icons.Default.Restaurant
        )
      }
    } else {
      items(activeOrders) { order ->
        MerchantOrderCard(order = order, merchantRepository = merchantRepository)
      }
    }

    // Completed Orders History Summary
    val completedOrders = orders.filter { it.status == MerchantOrderStatus.COMPLETED }
    if (completedOrders.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Riwayat Pesanan Selesai (${completedOrders.size})",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      items(completedOrders.take(3)) { order ->
        SukmaCard {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = "#${order.id} • ${order.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(text = order.itemsSummary, fontSize = 12.sp, color = SukmaSlateSubtle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
              text = "+ ${formatRupiah(order.netMerchantAmount)}",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = SukmaSuccessGreen
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun MerchantOrderCard(
  order: MerchantOrderEntity,
  merchantRepository: MerchantRepository
) {
  val scope = rememberCoroutineScope()
  var showRejectDialog by remember { mutableStateOf(false) }

  SukmaCard {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text(text = "#${order.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text(text = "Pemesan: ${order.customerName}", fontSize = 12.sp, color = SukmaSlateSubtle)
        }
        MerchantOrderStatusBadge(order.status)
      }

      HorizontalDivider()

      // Items ordered
      Text(
        text = order.itemsSummary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface
      )

      if (!order.customerNotes.isNullOrBlank()) {
        Surface(shape = ControlShape, color = SukmaSurgeLight, modifier = Modifier.fillMaxWidth()) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            Text(
              text = "Catatan: \"${order.customerNotes}\"",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = SukmaSurgeAmber
            )
          }
        }
      }

      PriceRow(
        label = "Pendapatan Bersih Resto",
        amount = formatRupiah(order.netMerchantAmount),
        color = SukmaEmeraldPrimary,
        weight = FontWeight.Bold,
        fontSize = 14.sp
      )

      // Driver Assignment Info if applicable
      if (order.driverName != null) {
        Surface(shape = ControlShape, color = SukmaMintLight, modifier = Modifier.fillMaxWidth()) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = null, tint = SukmaEmeraldPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Driver: ${order.driverName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SukmaEmeraldPrimary)
          }
        }
      }

      // Preparation Action Buttons
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        when (order.status) {
          MerchantOrderStatus.RECEIVED -> {
            TextButton(
              onClick = { showRejectDialog = true },
              modifier = Modifier.weight(1f)
            ) {
              Text("Tolak", color = SukmaEmergencyRed, fontWeight = FontWeight.Bold)
            }
            SukmaButton(
              text = "Konfirmasi & Masak (15 mnt)",
              onClick = {
                scope.launch {
                  merchantRepository.startCooking(order.id, 15)
                }
              },
              modifier = Modifier.weight(2f)
            )
          }
          MerchantOrderStatus.CONFIRMED, MerchantOrderStatus.PREPARING -> {
            SukmaButton(
              text = "Tandai Siap Diambil Driver",
              icon = Icons.Default.Check,
              onClick = {
                scope.launch {
                  merchantRepository.markReadyForPickup(order.id)
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
          MerchantOrderStatus.READY_FOR_PICKUP -> {
            SukmaButton(
              text = "Driver Sudah Mengambil • Serahkan",
              icon = Icons.Default.DeliveryDining,
              variant = SukmaButtonVariant.SECONDARY,
              onClick = {
                scope.launch {
                  merchantRepository.markPickedUpByDriver(order.id)
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
          MerchantOrderStatus.PICKED_UP -> {
            SukmaButton(
              text = "Konfirmasi Selesai & Klaim Saldo",
              icon = Icons.Default.CheckCircle,
              onClick = {
                scope.launch {
                  merchantRepository.completeOrder(order)
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
          else -> {}
        }
      }
    }
  }

  if (showRejectDialog) {
    var rejectReason by remember { mutableStateOf("Bahan makanan habis") }
    AlertDialog(
      onDismissRequest = { showRejectDialog = false },
      title = { Text("Tolak Pesanan #${order.id}", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Pilih atau tulis alasan pembatalan pesanan:", fontSize = 12.sp)
          listOf("Bahan makanan habis", "Dapur sedang over-kapasitas", "Toko segera tutup").forEach { r ->
            Surface(
              shape = PillShape,
              color = if (rejectReason == r) SukmaEmergencyRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .clickable { rejectReason = r }
            ) {
              Text(
                text = r,
                fontSize = 12.sp,
                fontWeight = if (rejectReason == r) FontWeight.Bold else FontWeight.Normal,
                color = if (rejectReason == r) SukmaEmergencyRed else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }
      },
      confirmButton = {
        SukmaButton(
          text = "Konfirmasi Tolak",
          variant = SukmaButtonVariant.DANGER,
          onClick = {
            scope.launch {
              merchantRepository.cancelOrder(order.id, rejectReason)
              showRejectDialog = false
            }
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showRejectDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
fun MerchantOrderStatusBadge(status: MerchantOrderStatus) {
  val (color, bgColor) = when (status) {
    MerchantOrderStatus.RECEIVED -> SukmaSurgeAmber to SukmaSurgeLight
    MerchantOrderStatus.CONFIRMED, MerchantOrderStatus.PREPARING -> SukmaEmeraldPrimary to SukmaEmeraldPrimary.copy(alpha = 0.12f)
    MerchantOrderStatus.READY_FOR_PICKUP -> SukmaMintAccent to SukmaMintLight
    MerchantOrderStatus.PICKED_UP -> Color(0xFF0284C7) to Color(0xFFE0F2FE)
    MerchantOrderStatus.COMPLETED -> SukmaSuccessGreen to SukmaSuccessLight
    MerchantOrderStatus.CANCELLED -> SukmaEmergencyRed to SukmaEmergencyRed.copy(alpha = 0.12f)
  }

  Surface(shape = PillShape, color = bgColor) {
    Text(
      text = status.label.uppercase(),
      color = color,
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
    )
  }
}

/**
 * Tab 2: Catalog, Categories, Products & Realtime Stock Management
 */
@Composable
fun MerchantCatalogTab(
  store: StoreEntity,
  categories: List<MerchantCategoryEntity>,
  products: List<MerchantProductEntity>,
  merchantRepository: MerchantRepository
) {
  val scope = rememberCoroutineScope()
  var selectedCategory by remember { mutableStateOf<String?>(null) }
  var showAddProductDialog by remember { mutableStateOf(false) }

  val filteredProducts = if (selectedCategory == null) {
    products
  } else {
    products.filter { it.categoryId == selectedCategory }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column {
            Text(
              text = "Katalog Menu & Stok",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${products.size} Menu Terdaftar",
              fontSize = 12.sp,
              color = SukmaSlateSubtle
            )
          }
          SukmaButton(
            text = "+ Tambah Menu",
            icon = Icons.Default.Add,
            onClick = { showAddProductDialog = true }
          )
        }
      }

      // Category Filter Row
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            val isAll = selectedCategory == null
            Surface(
              shape = PillShape,
              color = if (isAll) SukmaEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(PillShape)
                .clickable { selectedCategory = null }
            ) {
              Text(
                text = "Semua (${products.size})",
                fontSize = 12.sp,
                fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal,
                color = if (isAll) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
          items(categories) { cat ->
            val isSelected = selectedCategory == cat.id
            val count = products.count { it.categoryId == cat.id }
            Surface(
              shape = PillShape,
              color = if (isSelected) SukmaEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(PillShape)
                .clickable { selectedCategory = cat.id }
            ) {
              Text(
                text = "${cat.name} ($count)",
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // Products List
      if (filteredProducts.isEmpty()) {
        item {
          SukmaEmptyState(
            title = "Belum Ada Menu di Kategori Ini",
            description = "Klik tombol Tambah Menu untuk menambahkan hidangan ke katalog resto Anda.",
            icon = Icons.Default.Fastfood
          )
        }
      } else {
        items(filteredProducts) { prod ->
          MerchantProductCard(product = prod, merchantRepository = merchantRepository)
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  if (showAddProductDialog) {
    var nameInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var stockInput by remember { mutableStateOf("50") }
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "cat_main") }

    AlertDialog(
      onDismissRequest = { showAddProductDialog = false },
      title = { Text("Tambah Menu Baru", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Nama Makanan / Minuman") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it },
            label = { Text("Harga Jual (Rp)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = descInput,
            onValueChange = { descInput = it },
            label = { Text("Deskripsi Porsi / Rasa") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = stockInput,
            onValueChange = { stockInput = it },
            label = { Text("Jumlah Stok Tersedia") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        SukmaButton(
          text = "Simpan Menu",
          enabled = nameInput.isNotBlank() && priceInput.isNotBlank(),
          onClick = {
            scope.launch {
              val price = priceInput.toLongOrNull() ?: 20000L
              val stock = stockInput.toIntOrNull() ?: 50
              merchantRepository.addProduct(
                storeId = store.id,
                categoryId = selectedCatId,
                name = nameInput.trim(),
                description = descInput.trim(),
                price = price,
                stockQuantity = stock
              )
              showAddProductDialog = false
            }
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showAddProductDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
fun MerchantProductCard(
  product: MerchantProductEntity,
  merchantRepository: MerchantRepository
) {
  val scope = rememberCoroutineScope()

  SukmaCard {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text(text = formatRupiah(product.price), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SukmaEmeraldPrimary)
        }
        // Availability Toggle Switch
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = if (product.isAvailable && product.stockQuantity > 0) "Tersedia" else "Habis",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (product.isAvailable && product.stockQuantity > 0) SukmaSuccessGreen else SukmaEmergencyRed
          )
          Spacer(modifier = Modifier.width(6.dp))
          Switch(
            checked = product.isAvailable && product.stockQuantity > 0,
            onCheckedChange = { isAvailable ->
              scope.launch {
                merchantRepository.toggleProductAvailability(product.id, isAvailable)
              }
            }
          )
        }
      }

      Text(
        text = product.description,
        fontSize = 12.sp,
        color = SukmaSlateSubtle,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      HorizontalDivider()

      // Stock Controls (+ / -)
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Stok Sisa: ${product.stockQuantity} porsi",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (product.stockQuantity < 10) SukmaEmergencyRed else MaterialTheme.colorScheme.onSurface
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .clickable {
                scope.launch {
                  val newStock = maxOf(0, product.stockQuantity - 1)
                  merchantRepository.updateStock(product.id, newStock)
                }
              }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = "-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }

          Text(
            text = "${product.stockQuantity}",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
          )

          Surface(
            shape = CircleShape,
            color = SukmaEmeraldPrimary.copy(alpha = 0.15f),
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .clickable {
                scope.launch {
                  merchantRepository.updateStock(product.id, product.stockQuantity + 5)
                }
              }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(text = "+5", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SukmaEmeraldPrimary)
            }
          }
        }
      }
    }
  }
}

/**
 * Tab 3: Finance, Settlement & Ledger
 */
@Composable
fun MerchantFinanceTab(
  store: StoreEntity,
  balance: Long,
  ledgerEntries: List<com.example.core.database.entity.LedgerEntryEntity>,
  merchantRepository: MerchantRepository
) {
  val scope = rememberCoroutineScope()
  var showPayoutDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      // Hero Card for Store Earnings
      Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SukmaSlateDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(text = "Total Saldo Pendapatan Toko", fontSize = 13.sp, color = Color(0xFF94A3B8))
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = formatRupiah(balance), fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = "Potongan Komisi Platform: ${store.commissionRatePercent}%", fontSize = 11.sp, color = SukmaMintAccent)
          Spacer(modifier = Modifier.height(14.dp))
          SukmaButton(
            text = "Tarik Saldo ke Rekening Bank (Settlement)",
            icon = Icons.Default.AccountBalance,
            onClick = { showPayoutDialog = true }
          )
        }
      }
    }

    item {
      Text(
        text = "Riwayat Buku Besar Toko",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    if (ledgerEntries.isEmpty()) {
      item {
        SukmaEmptyState(
          title = "Belum Ada Mutasi Keuangan",
          description = "Setiap penjualan makanan dan pencairan saldo resto akan tercatat di buku besar ini.",
          icon = Icons.Default.History
        )
      }
    } else {
      items(ledgerEntries) { entry ->
        SukmaCard {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = entry.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              Text(
                text = "Ref: ${entry.referenceId} • ${entry.reason.label}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            val isCredit = entry.direction == LedgerDirection.CREDIT
            Text(
              text = "${if (isCredit) "+" else "-"} ${formatRupiah(entry.amount)}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (isCredit) SukmaSuccessGreen else SukmaEmergencyRed
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  if (showPayoutDialog) {
    var payoutAmount by remember { mutableStateOf(100000L) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showPayoutDialog = false },
      title = { Text("Pencairan Saldo Toko (Settlement)", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Rekening Pencairan Toko:", fontSize = 12.sp, color = SukmaSlateSubtle)
          Text("${store.bankName} - ${store.bankAccountNumber}\na.n ${store.bankAccountHolder}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
          Text("Pilih Nominal Pencairan:", fontSize = 12.sp)
          listOf(50000L, 100000L, 200000L, balance).filter { it > 0 }.distinct().forEach { amt ->
            Surface(
              shape = ControlShape,
              color = if (payoutAmount == amt) SukmaEmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, if (payoutAmount == amt) SukmaEmeraldPrimary else MaterialTheme.colorScheme.outline),
              modifier = Modifier
                .fillMaxWidth()
                .clip(ControlShape)
                .clickable { payoutAmount = amt }
            ) {
              Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(12.dp)) {
                Text(text = if (amt == balance) "Tarik Semua (${formatRupiah(amt)})" else formatRupiah(amt), fontWeight = FontWeight.Bold)
                if (payoutAmount == amt) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SukmaEmeraldPrimary)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        SukmaButton(
          text = "Konfirmasi Tarik (${formatRupiah(payoutAmount)})",
          isLoading = isSubmitting,
          onClick = {
            isSubmitting = true
            scope.launch {
              merchantRepository.requestSettlement(
                merchantId = store.id,
                amount = payoutAmount,
                bankInfo = "${store.bankName} ${store.bankAccountNumber}"
              )
              isSubmitting = false
              showPayoutDialog = false
            }
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showPayoutDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}

/**
 * Tab 4: Store Profile, Business Legal & Onboarding / KYC
 */
@Composable
fun MerchantStoreKycTab(
  store: StoreEntity,
  merchantRepository: MerchantRepository
) {
  val scope = rememberCoroutineScope()
  var showEditKycDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      SukmaCard {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = CircleShape,
            color = SukmaEmeraldPrimary.copy(alpha = 0.15f),
            modifier = Modifier.size(56.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = SukmaEmeraldPrimary, modifier = Modifier.size(30.dp))
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = store.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = store.category, fontSize = 12.sp, color = SukmaSlateSubtle)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(shape = PillShape, color = SukmaSuccessLight) {
              Text(
                text = "MITRA MERCHANT RESMI",
                color = SukmaSuccessGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }
      }
    }

    item {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Kelengkapan Dokumen Usaha (KYC)",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = { showEditKycDialog = true }) {
          Text("Ubah Data", color = SukmaEmeraldPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }

    item {
      MerchantKycDocItem("Nomor Pokok Wajib Pajak (NPWP)", store.npwpNumber, "NPWP Terdaftar Aktif")
    }
    item {
      MerchantKycDocItem("Nomor Induk Berusaha (NIB)", store.nibNumber, "Izin Usaha Resto Valid")
    }
    item {
      MerchantKycDocItem("KTP Penanggung Jawab Resto", store.ktpNumber, "Verifikasi Dukcapil OK")
    }
    item {
      MerchantKycDocItem("Alamat Lengkap Gerai / Dapur", store.address, "Zona: ${store.zoneId}")
    }
    item {
      MerchantKycDocItem("Rekening Bank Pencairan Pendapatan", "${store.bankName} - ${store.bankAccountNumber}\na.n ${store.bankAccountHolder}", "Rekening Utama")
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  if (showEditKycDialog) {
    var nameInput by remember { mutableStateOf(store.name) }
    var descInput by remember { mutableStateOf(store.description) }
    var addressInput by remember { mutableStateOf(store.address) }
    var phoneInput by remember { mutableStateOf(store.phone) }
    var bankInput by remember { mutableStateOf(store.bankName) }
    var accInput by remember { mutableStateOf(store.bankAccountNumber) }
    var holderInput by remember { mutableStateOf(store.bankAccountHolder) }
    var npwpInput by remember { mutableStateOf(store.npwpNumber) }
    var nibInput by remember { mutableStateOf(store.nibNumber) }

    AlertDialog(
      onDismissRequest = { showEditKycDialog = false },
      title = { Text("Perbarui Informasi Resto & KYC", fontWeight = FontWeight.Bold) },
      text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          item { OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Nama Toko / Resto") }, modifier = Modifier.fillMaxWidth()) }
          item { OutlinedTextField(value = descInput, onValueChange = { descInput = it }, label = { Text("Deskripsi Toko") }, modifier = Modifier.fillMaxWidth()) }
          item { OutlinedTextField(value = addressInput, onValueChange = { addressInput = it }, label = { Text("Alamat Resto") }, modifier = Modifier.fillMaxWidth()) }
          item { OutlinedTextField(value = phoneInput, onValueChange = { phoneInput = it }, label = { Text("Nomor Telepon") }, modifier = Modifier.fillMaxWidth()) }
          item { OutlinedTextField(value = bankInput, onValueChange = { bankInput = it }, label = { Text("Nama Bank") }, modifier = Modifier.fillMaxWidth()) }
          item { OutlinedTextField(value = accInput, onValueChange = { accInput = it }, label = { Text("Nomor Rekening") }, modifier = Modifier.fillMaxWidth()) }
          item { OutlinedTextField(value = holderInput, onValueChange = { holderInput = it }, label = { Text("Nama Pemilik Rekening") }, modifier = Modifier.fillMaxWidth()) }
          item { OutlinedTextField(value = npwpInput, onValueChange = { npwpInput = it }, label = { Text("NPWP Resto") }, modifier = Modifier.fillMaxWidth()) }
          item { OutlinedTextField(value = nibInput, onValueChange = { nibInput = it }, label = { Text("NIB Usaha") }, modifier = Modifier.fillMaxWidth()) }
        }
      },
      confirmButton = {
        SukmaButton(
          text = "Simpan Data",
          onClick = {
            scope.launch {
              merchantRepository.updateStoreKyc(
                storeId = store.id,
                name = nameInput,
                desc = descInput,
                address = addressInput,
                phone = phoneInput,
                bankName = bankInput,
                accountNum = accInput,
                accountHolder = holderInput,
                npwp = npwpInput,
                nib = nibInput
              )
              showEditKycDialog = false
            }
          }
        )
      },
      dismissButton = {
        TextButton(onClick = { showEditKycDialog = false }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
fun MerchantKycDocItem(title: String, value: String, status: String) {
  SukmaCard {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, fontSize = 12.sp, color = SukmaSlateSubtle)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = status, fontSize = 11.sp, color = SukmaSuccessGreen, fontWeight = FontWeight.SemiBold)
      }
      Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "OK", tint = SukmaSuccessGreen)
    }
  }
}
