import SwiftUI

// BioCue-derived calm palette for Contexts — identical tokens to BioCue DLS.
// Surfaces are warm and soft; colour is reserved for meaning (calm / rising / alert / info).
enum Palette {
    // Surfaces
    static let bg = DlsTokens.surfaceBg
    static let card = DlsTokens.surfaceCard
    static let surface2 = DlsTokens.surfaceRaised
    static let line = DlsTokens.borderSubtle
    static let lineStrong = DlsTokens.borderStrong

    // Inks
    static let inkPrimary = DlsTokens.textPrimary
    static let inkSecondary = DlsTokens.textSecondary
    static let inkMuted = DlsTokens.textMuted

    // Primary action
    static let charcoal = DlsTokens.actionPrimary
    static let onCharcoal = DlsTokens.actionOnPrimary
    static let actionPrimary = DlsTokens.actionPrimary
    static let surfaceCard = DlsTokens.surfaceCard
    static let surfaceRaised = DlsTokens.surfaceRaised
    static let borderSubtle = DlsTokens.borderSubtle
}

struct ContextIdentifiable: Identifiable {
    let id: String
}

extension Color {
    init(hex: String) {
        let cleanHex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: cleanHex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch cleanHex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}


/// A chip / soft-background / ink triple for one emotional / cognitive state.
struct StatePalette {
    let chip: Color
    let soft: Color
    let ink: Color

    static let calm = StatePalette(chip: DlsTokens.stateCalmChip, soft: DlsTokens.stateCalmSoft, ink: DlsTokens.stateCalmInk)
    static let rising = StatePalette(chip: DlsTokens.stateRisingChip, soft: DlsTokens.stateRisingSoft, ink: DlsTokens.stateRisingInk)
    static let alert = StatePalette(chip: DlsTokens.stateAlertChip, soft: DlsTokens.stateAlertSoft, ink: DlsTokens.stateAlertInk)
    static let info = StatePalette(chip: DlsTokens.stateInfoChip, soft: DlsTokens.stateInfoSoft, ink: DlsTokens.stateInfoInk)
    static let neutral = StatePalette(chip: DlsTokens.stateNeutralChip, soft: DlsTokens.stateNeutralSoft, ink: DlsTokens.stateNeutralInk)
}

enum Radius {
    static let sm: CGFloat = DlsScales.radiusSm
    static let md: CGFloat = DlsScales.radiusMd
    static let card: CGFloat = DlsScales.radiusCard
    static let lg: CGFloat = DlsScales.radiusLg
    static let xl: CGFloat = DlsScales.radiusXl
    static let pill: CGFloat = 999
}

enum Metrics {
    static let minTouch: CGFloat = DlsScales.sizeTouchTarget
    static let buttonHeight: CGFloat = DlsScales.sizeButtonHeight
}

enum AppFont {
    static let displayMedium = Font.system(size: DlsType.displayMediumSize, weight: .medium, design: .serif)
    static let headlineLarge = Font.system(size: DlsType.headlineLargeSize, weight: .medium, design: .serif)
    static let headlineSmall = Font.system(size: DlsType.headlineSmallSize, weight: .medium, design: .serif)
    static let titleLarge = Font.system(size: DlsType.titleLargeSize, weight: .medium, design: .serif)
    static let titleMedium = Font.system(size: DlsType.titleMediumSize, weight: .bold, design: .default)
    static let bodyLarge = Font.system(size: DlsType.bodyLargeSize, weight: .regular, design: .default)
    static let bodyMedium = Font.system(size: DlsType.bodyMediumSize, weight: .regular, design: .default)
    static let bodySmall = Font.system(size: DlsType.bodySmallSize, weight: .semibold, design: .default)
    static let labelLarge = Font.system(size: DlsType.labelLargeSize, weight: .bold, design: .default)
    static let labelMedium = Font.system(size: DlsType.labelMediumSize, weight: .bold, design: .default)
    static let labelSmall = Font.system(size: DlsType.labelSmallSize, weight: .bold, design: .default)
}

extension View {
    func calmBackground() -> some View {
        self.frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Palette.bg.ignoresSafeArea())
    }
}
