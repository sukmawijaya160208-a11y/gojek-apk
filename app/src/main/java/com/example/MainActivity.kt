package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.core.database.AppDatabase
import com.example.core.database.SeedData
import com.example.core.model.UserRole
import com.example.core.repository.AuthRepository
import com.example.core.repository.DriverRepository
import com.example.core.repository.MerchantRepository
import com.example.core.repository.OrderRepository
import com.example.core.repository.WalletRepository
import com.example.core.ui.components.SukmaTopBar
import com.example.features.admin.AdminHomeScreen
import com.example.features.common.RoleSwitchDialog
import com.example.features.customer.CustomerActivityScreen
import com.example.features.customer.CustomerHomeScreen
import com.example.features.customer.CustomerWalletScreen
import com.example.features.driver.DriverHomeScreen
import com.example.features.merchant.MerchantHomeScreen
import com.example.features.ops.OpsHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SukmaEmeraldPrimary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class CustomerTab(val label: String, val icon: ImageVector) {
  HOME("Beranda", Icons.Default.Home),
  ACTIVITY("Aktivitas", Icons.Default.History),
  WALLET("Dompet", Icons.Default.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {

  private lateinit var database: AppDatabase
  private lateinit var authRepository: AuthRepository
  private lateinit var orderRepository: OrderRepository
  private lateinit var walletRepository: WalletRepository
  private lateinit var driverRepository: DriverRepository
  private lateinit var merchantRepository: MerchantRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    database = AppDatabase.getInstance(this)
    authRepository = AuthRepository(database)
    orderRepository = OrderRepository(database)
    walletRepository = WalletRepository(database)
    driverRepository = DriverRepository(database)
    merchantRepository = MerchantRepository(database)

    // Seed database if first launch
    lifecycleScope.launch {
      val users = database.userDao().getAllUsers().first()
      if (users.isEmpty()) {
        SeedData.populateDatabase(database)
      }
    }

    setContent {
      MyApplicationTheme {
        MainRootApp(
          authRepository = authRepository,
          orderRepository = orderRepository,
          walletRepository = walletRepository,
          driverRepository = driverRepository,
          merchantRepository = merchantRepository
        )
      }
    }
  }
}

@Composable
fun MainRootApp(
  authRepository: AuthRepository,
  orderRepository: OrderRepository,
  walletRepository: WalletRepository,
  driverRepository: DriverRepository,
  merchantRepository: MerchantRepository
) {
  val scope = rememberCoroutineScope()
  val currentUser by authRepository.currentUser.collectAsState()
  var showRoleSwitcher by remember { mutableStateOf(false) }
  var customerSelectedTab by remember { mutableStateOf(CustomerTab.HOME) }

  Scaffold(
    topBar = {
      val title = when (currentUser.role) {
        UserRole.CUSTOMER -> "Go Sukma Drive"
        UserRole.DRIVER -> "Mitra Driver Go Sukma"
        UserRole.MERCHANT -> "Mitra Merchant Resto"
        UserRole.OPS -> "Dispatcher Control"
        UserRole.ADMIN, UserRole.SUPER_ADMIN -> "Pusat Kendali Admin"
        else -> "Go Sukma Drive"
      }
      val subtitle = when (currentUser.role) {
        UserRole.CUSTOMER -> "Super App Mobilitas & Pengantaran"
        UserRole.DRIVER -> currentUser.fullName
        UserRole.MERCHANT -> "Dapur Nusantara Resto"
        UserRole.OPS -> "Fleet Live Telemetry"
        UserRole.ADMIN, UserRole.SUPER_ADMIN -> "Super Admin Platform"
        else -> null
      }

      SukmaTopBar(
        title = title,
        subtitle = subtitle,
        currentRole = currentUser.role,
        onSwitchRoleClick = { showRoleSwitcher = true }
      )
    },
    bottomBar = {
      // Bottom Navigation only shown for Customer surface
      if (currentUser.role == UserRole.CUSTOMER) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 4.dp,
          modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
          CustomerTab.values().forEach { tab ->
            val isSelected = customerSelectedTab == tab
            NavigationBarItem(
              selected = isSelected,
              onClick = { customerSelectedTab = tab },
              icon = {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = tab.label
                )
              },
              label = {
                Text(
                  text = tab.label,
                  fontSize = 12.sp
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
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      when (currentUser.role) {
        UserRole.CUSTOMER -> {
          when (customerSelectedTab) {
            CustomerTab.HOME -> {
              CustomerHomeScreen(
                currentUser = currentUser,
                orderRepository = orderRepository,
                walletRepository = walletRepository,
                onOpenWallet = { customerSelectedTab = CustomerTab.WALLET },
                onOpenActivity = { customerSelectedTab = CustomerTab.ACTIVITY }
              )
            }
            CustomerTab.ACTIVITY -> {
              CustomerActivityScreen(
                currentUser = currentUser,
                orderRepository = orderRepository
              )
            }
            CustomerTab.WALLET -> {
              CustomerWalletScreen(
                walletRepository = walletRepository
              )
            }
          }
        }
        UserRole.DRIVER -> {
          DriverHomeScreen(
            currentUser = currentUser,
            driverRepository = driverRepository,
            orderRepository = orderRepository,
            walletRepository = walletRepository
          )
        }
        UserRole.MERCHANT -> {
          MerchantHomeScreen(
            merchantRepository = merchantRepository,
            walletRepository = walletRepository
          )
        }
        UserRole.OPS -> {
          OpsHomeScreen()
        }
        UserRole.ADMIN, UserRole.SUPER_ADMIN -> {
          AdminHomeScreen(
            orderRepository = orderRepository,
            authRepository = authRepository
          )
        }
        else -> {
          CustomerHomeScreen(
            currentUser = currentUser,
            orderRepository = orderRepository,
            walletRepository = walletRepository,
            onOpenWallet = { customerSelectedTab = CustomerTab.WALLET },
            onOpenActivity = { customerSelectedTab = CustomerTab.ACTIVITY }
          )
        }
      }
    }
  }

  if (showRoleSwitcher) {
    RoleSwitchDialog(
      currentRole = currentUser.role,
      onRoleSelected = { role ->
        scope.launch {
          authRepository.switchUserRole(role)
        }
      },
      onDismiss = { showRoleSwitcher = false }
    )
  }
}
