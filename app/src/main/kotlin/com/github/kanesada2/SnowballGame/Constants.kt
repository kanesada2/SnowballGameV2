package com.github.kanesada2.SnowballGame

import org.bukkit.util.Vector

/**
 * SnowballGame で使用される定数を一元管理するオブジェクト
 */
object Constants {

    // ========================================
    // Ball Physics - ボール物理演算
    // ========================================
    object BallPhysics {
        /** デフォルトのバウンド時反発係数ベクトル (x, y, z) */
        val DEFAULT_REPULSION = Vector(0.7, 0.4, 0.7)

        /** 審判検出範囲 (x, y, z) */
        const val UMPIRE_DETECT_RANGE_X = 50.0
        const val UMPIRE_DETECT_RANGE_Y = 10.0
        const val UMPIRE_DETECT_RANGE_Z = 50.0

        /** 同じ場所でのバウンス最大回数 */
        const val MAX_SAME_PLACE_BOUNCE = 5

        /** 最大バウンス回数（これを超えるとアイテム化） */
        const val MAX_BOUNCE_COUNT = 50

        /** 同じ場所判定の距離閾値（ブロック単位の二乗） */
        const val SAME_PLACE_DISTANCE_SQUARED = 1.0

        /** 通過ブロック時の垂直オフセット */
        const val PASSTHROUGH_VERTICAL_OFFSET = 0.1

        /** スピン毎tick減衰率（空気抵抗） */
        const val SPIN_DECAY_PER_TICK = 0.99

        /** ランダム方向変化のステップ */
        const val RANDOM_DIRECTION_STEP = 0.3

        /** バウンス時のエネルギー減衰指数ベース */
        const val BOUNCE_ENERGY_DECAY_BASE = 1.3

        /** スピン効果の乗数 */
        const val SPIN_EFFECT_MULTIPLIER = 9

        /** 直角変換用（90度のラジアン） */
        val RIGHT_ANGLE_RADIANS = Math.toRadians(90.0)

        /** スピン係数乗数（バウンス時） */
        const val SPIN_COEFFICIENT_ON_BOUNCE = 0.01

        /** 地面と水平な移動量からスピンへの変換係数 */
        const val LINEAR_TO_SPIN_COEFFICIENT = 0.003

        /** 速度閾値（これ以下でアイテム化） */
        const val VELOCITY_DROP_THRESHOLD = 0.15

        /** 転がり開始のY方向速度閾値 */
        const val ROLLING_VELOCITY_THRESHOLD = 0.15
    }

    // ========================================
    // Ball Rolling - ボール転がり
    // ========================================
    object BallRolling {
        /** 転がり最大時間（tick, 600 = 30秒） */
        const val MAX_ROLLING_TIME = 600

        /** 転がり停止の速度閾値 */
        const val STOP_VELOCITY_THRESHOLD = 0.05

        /** 表面検出オフセット（浅い） */
        const val SURFACE_CHECK_OFFSET_SHALLOW = -0.15

        /** 表面検出オフセット（1ブロック下） */
        const val SURFACE_CHECK_OFFSET_ONE_BLOCK = -1.0

        /** 表面検出オフセット（深い） */
        const val SURFACE_CHECK_OFFSET_DEEP = -1.15

        /** 転がり摩擦係数（毎tick） */
        const val ROLLING_FRICTION = 0.98
    }

    // ========================================
    // Ball Judge - ボール判定
    // ========================================
    object BallJudge {
        /** ストライクゾーン判定を待機する最大tick数 */
        const val MAX_JUDGE_COUNT = 100

        /** 判定分解能（1tick を何分割するか） */
        const val JUDGE_RESOLUTION = 0.1

        /** 速度→km/h 変換係数 */
        const val VELOCITY_TO_KMH = 72
    }

    // ========================================
    // Swing Gauge - スイングゲージ
    // ========================================
    object SwingGauge {
        /** チャージ完了までのtick数（1秒） */
        const val CHARGE_TICKS = 20

        /** 満タン維持のtick数 */
        const val FULL_HOLD_TICKS = 24

        /** リセット時の初期値 */
        const val RESET_PROGRESS = 0.05
    }

    // ========================================
    // Batter - バッター
    // ========================================
    object Batter {
        /** デフォルトの打球速度計算用係数 */
        const val DEFAULT_SWING_RATE = 1.3

        /** デフォルトのスイング係数 */
        const val DEFAULT_SWING_COEFFICIENT = 1.0

        /** スイングクールダウン（tick） */
        const val SWING_COOLDOWN_TICKS = 1

        /** 初期ボスバー進捗 */
        const val INITIAL_BAR_PROGRESS = 0.0

        /** スイング強度による範囲の係数 */
        const val HIT_RANGE_MODIFIER = 0.5

        /** 強打パーティクル閾値 */
        const val STRONG_SWING_THRESHOLD = 0.7

        /** スイングアーク刻み幅（ラジアン, π/20） */
        const val SWING_ARC_STEP = 0.15708

        /** スイングを取得するためのインパクト位置の差分 */
        const val IMPACT_ADJUSTMENT = 0.01

        /** スニーク時にスイング強度にかける倍率 */
        const val SWING_POWER_SNEAK_AMPLIFIER = 0.5

        /** 非スニーク時にプラスするスイング強度の基準値 */
        const val SWING_POWER_ADJUSTMENT = 0.5
    }

    // ========================================
    // Fielder - 野手
    // ========================================
    object Fielder {
        /** デフォルトの捕球成功係数 */
        const val DEFAULT_CATCH_RATE = 0.3

        /** 捕球試行後のクールダウン（tick） */
        const val CATCH_COOLDOWN_TICKS = 5

        /** ファンブルしたボールの速度係数 */
        const val MISS_SCATTER_FACTOR = 0.3
    }

    // ========================================
    // Coach (Knocker) - コーチ（ノッカー）
    // ========================================
    object Coach {
        /** 縦方向ランダム係数の距離除数 */
        const val VERTICAL_RANDOM_DISTANCE_DIVISOR = 30

        /** パワー調整ベース（高さ依存） */
        const val POWER_ADJUSTMENT_BASE = 2.2

        /** 横方向ランダム係数の距離除数 */
        const val HORIZONTAL_RANDOM_DISTANCE_DIVISOR = 8

        /** 極端な角度の閾値（ラジアン） */
        const val EXTREME_ANGLE_THRESHOLD = 0.5

        /** 極端な角度時のパワー低減 */
        const val EXTREME_ANGLE_POWER_REDUCTION = 0.7

        /** フライ判定する打球角度の境目（度） */
        const val STEEP_ANGLE_DEGREES = 30

        /** パワー調整指数 */
        const val POWER_ADJUSTMENT_EXPONENT = 2.0

        /** 飛距離計算の距離除数 */
        const val FLIGHT_DISTANCE_DIVISOR = 25

        /** 基本スピン係数 */
        const val BASE_SPIN_MAGNITUDE = 0.005

        /** スピン速度指数ベース */
        const val SPIN_VELOCITY_EXPONENT_BASE = 2.0
    }

    // ========================================
    // Hit Processing - ヒット処理
    // ========================================
    object HitProcessing {
        /** 元速度の減衰係数 */
        const val ORIGINAL_VELOCITY_DAMPING = -0.3

        /** ヒット方向乗数 */
        const val HIT_DIRECTION_MULTIPLIER = 2

        /** バックスピン成分乗数 */
        const val BACKSPIN_MULTIPLIER = 2

        /** 芯からの距離に応じたスピンの係数 */
        const val OFF_CENTER_SPIN_COEFFICIENT = 0.01
    }

    // ========================================
    // Ball Attributes - ボール属性
    // ========================================
    object BallAttributes {
        /** ディスペンサー縦方向補正係数 */
        const val DISPENSER_VERTICAL_CORRECTION = 0.9

        /** ディスペンサー横方向補正係数 */
        const val DISPENSER_HORIZONTAL_CORRECTION = 0.65

        /** ディスペンサー補正強度 */
        const val DISPENSER_CORRECTION_INTENSITY = 15
    }

    // ========================================
    // Glove Attributes - グラブ属性
    // ========================================
    object GloveAttributes {
        /** 基本横オフセット */
        const val BASE_HORIZONTAL_OFFSET = 0.2

        /** 速度修正乗数 */
        const val VELOCITY_MODIFIER_MULTIPLIER = -0.1
    }

    // ========================================
    // Particles & Effects - パーティクル・エフェクト
    // ========================================
    object Effects {
        /** 移動ボールのパーティクル数 */
        const val MOVING_BALL_PARTICLE_COUNT = 5

        /** パーティクルの拡散範囲 */
        const val PARTICLE_SPREAD = 0.5

        /** 投球時のサウンド音量 */
        const val THROW_SOUND_VOLUME = 0.5f

        /** バウンド時のエフェクト半径 */
        const val BOUNCE_EFFECT_RADIUS = 1.5f
    }

    // ========================================
    // Commands - コマンド
    // ========================================
    object Commands {
        /** sweepコマンドの範囲（ブロック） */
        const val SWEEP_RANGE = 3.0
    }

    // ========================================
    // Misc - その他
    // ========================================
    object Misc {
        /** 雪だるま検出範囲 */
        const val SNOWMAN_DETECT_RANGE = 0.5

        /** スイング/バント判定の力閾値 */
        const val SWING_FORCE_THRESHOLD = 0.4

        /** 位置補正の最大探索深度 */
        const val MAX_CORRECTION_SEARCH_DEPTH = 5
    }
}