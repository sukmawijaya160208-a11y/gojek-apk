package com.example.core.engine

import com.example.core.database.AppDatabase
import com.example.core.database.entity.PricingPolicyEntity
import com.example.core.database.entity.PromoEntity
import com.example.core.model.ServiceType

data class PricingQuote(
  val serviceType: ServiceType,
  val distanceKm: Double,
  val durationMin: Int,
  val baseFare: Long,
  val distanceFare: Long,
  val timeFare: Long,
  val subtotal: Long,
  val platformFee: Long,
  val surgeAmount: Long = 0L,
  val discountAmount: Long = 0L,
  val totalFare: Long,
  val policyVersion: String = "v1.1-JKT",
  val subtotalBeforeSurge: Long = maxOf(0L, subtotal - surgeAmount),
  val surgeMultiplier: Double = 1.0,
  val netDriverEarnings: Long = maxOf(0L, totalFare - platformFee),
  val zoneId: String = "ZONE_CENTRAL",
  val isSurgeActive: Boolean = surgeMultiplier > 1.0 || surgeAmount > 0,
  val surgeLabel: String? = if (surgeMultiplier > 1.0) "Tarif Ramai ${surgeMultiplier}x" else null
)

object PricingEngine {

  /**
   * Computes deterministic price quote considering distance, duration, zone policy, surge multiplier,
   * platform fee, and promotional vouchers.
   */
  suspend fun calculateQuote(
    db: AppDatabase,
    serviceType: ServiceType,
    distanceKm: Double,
    promoCode: String? = null,
    zoneId: String = "ZONE_CENTRAL",
    isPeakHour: Boolean = false
  ): PricingQuote {
    val durationMin = GeoRoutingEngine.estimateDurationMinutes(distanceKm, serviceType, isPeakHour)

    // 1. Fetch zone policy or use defaults
    val policy: PricingPolicyEntity? = try {
      db.pricingDao().getPolicySync(zoneId, serviceType)
    } catch (_: Exception) {
      null
    }

    val baseFare = policy?.baseFare ?: when (serviceType) {
      ServiceType.RIDE_BIKE -> 8000L
      ServiceType.RIDE_CAR -> 16000L
      ServiceType.DELIVERY_BIKE -> 9000L
      ServiceType.FOOD -> 7000L
      ServiceType.SHOP -> 8000L
    }

    val distanceRate = policy?.distanceRateKm ?: when (serviceType) {
      ServiceType.RIDE_BIKE -> 2500L
      ServiceType.RIDE_CAR -> 5000L
      ServiceType.DELIVERY_BIKE -> 2800L
      ServiceType.FOOD -> 2200L
      ServiceType.SHOP -> 2400L
    }

    val timeRate = policy?.timeRateMin ?: when (serviceType) {
      ServiceType.RIDE_BIKE -> 300L
      ServiceType.RIDE_CAR -> 600L
      else -> 200L
    }

    val platformFee = policy?.platformFee ?: when (serviceType) {
      ServiceType.RIDE_CAR -> 4000L
      else -> 2000L
    }

    val minFare = policy?.minimumFare ?: when (serviceType) {
      ServiceType.RIDE_BIKE -> 12000L
      ServiceType.RIDE_CAR -> 22000L
      else -> 12000L
    }

    val surgeMultiplier = policy?.surgeMultiplier ?: 1.0

    // 2. Base + Distance + Time computation
    val distanceFare = (distanceKm * distanceRate).toLong()
    val timeFare = (durationMin * timeRate).toLong()
    val subtotalBeforeSurge = baseFare + distanceFare + timeFare

    // 3. Surge multiplier application
    val surgedCalculated = (subtotalBeforeSurge * surgeMultiplier).toLong()
    val surgeAmount = maxOf(0L, surgedCalculated - subtotalBeforeSurge)
    val subtotal = maxOf(minFare, surgedCalculated)

    // 4. Promo code evaluation
    var discount = 0L
    if (!promoCode.isNullOrBlank()) {
      val promo: PromoEntity? = db.pricingDao().getPromoByCode(promoCode.trim().uppercase())
      if (promo != null && subtotal >= promo.minOrderAmount) {
        val calcDiscount = (subtotal * promo.discountPercentage) / 100
        discount = minOf(calcDiscount, promo.maxDiscountAmount)
      }
    }

    // 5. Final customer total fare
    val totalFare = maxOf(minFare, subtotal + platformFee - discount)

    // 6. Net Driver earnings (Total Fare - Platform Fee)
    val netDriverEarnings = maxOf(0L, totalFare - platformFee)

    return PricingQuote(
      serviceType = serviceType,
      distanceKm = distanceKm,
      durationMin = durationMin,
      baseFare = baseFare,
      distanceFare = distanceFare,
      timeFare = timeFare,
      subtotal = subtotal,
      platformFee = platformFee,
      surgeAmount = surgeAmount,
      discountAmount = discount,
      totalFare = totalFare,
      policyVersion = policy?.policyVersion ?: "v1.1-JKT",
      subtotalBeforeSurge = subtotalBeforeSurge,
      surgeMultiplier = surgeMultiplier,
      netDriverEarnings = netDriverEarnings,
      zoneId = zoneId
    )
  }
}
