#!/usr/bin/env swift
import AppKit
import Foundation

let rootDir = FileManager.default.currentDirectoryPath

struct ScreenshotSpec {
    let filename: String
    let srcFile: String
    let tag: String
    let title: String
    let subtitle: String
}

let specs: [ScreenshotSpec] = [
    ScreenshotSpec(
        filename: "01_landing_brand.png",
        srcFile: "\(rootDir)/build/reports/ios_reviewer_tests/step01_landing_screen.png",
        tag: "BioCue DLS · Zero Cloud",
        title: "Unstick Your Thoughts",
        subtitle: "Serene mental flow when your mind feels overloaded"
    ),
    ScreenshotSpec(
        filename: "02_timeline_board.png",
        srcFile: "\(rootDir)/build/reports/ios_reviewer_tests/step02_board_guest.png",
        tag: "Unified Timeline",
        title: "Vertical Schedule Flow",
        subtitle: "Day selector strip & 7-tier pinch-to-zoom timeline"
    ),
    ScreenshotSpec(
        filename: "03_cognitive_load_limiter.png",
        srcFile: "\(rootDir)/build/reports/ios_reviewer_tests/step03_limiter_calm.png",
        tag: "Cognitive Load Cap",
        title: "Balance Your Headspace",
        subtitle: "Know exactly what's in your head before taking on more"
    ),
    ScreenshotSpec(
        filename: "04_capacity_overflow_warning.png",
        srcFile: "\(rootDir)/build/reports/ios_reviewer_tests/step04_limiter_overflow.png",
        tag: "Mindful Thresholds",
        title: "Cognitive Overflow Alert",
        subtitle: "Gentle visual alert guiding you to park or close thoughts"
    ),
    ScreenshotSpec(
        filename: "05_new_thought_sheet.png",
        srcFile: "\(rootDir)/build/reports/ios_reviewer_tests/step05_context_creation_sheet.png",
        tag: "Capture Instantly",
        title: "Tactile Context Sheets",
        subtitle: "Define stage, date, spanning banner, and checklists"
    ),
    ScreenshotSpec(
        filename: "06_deep_focus_session.png",
        srcFile: "\(rootDir)/build/reports/ios_reviewer_tests/step06_focus_session.png",
        tag: "Deep Work Session",
        title: "Distraction-Free Focus",
        subtitle: "Live stopwatch timer to protect single-task attention"
    ),
    ScreenshotSpec(
        filename: "07_profile_and_limits.png",
        srcFile: "\(rootDir)/build/reports/ios_reviewer_tests/step07_profile_limit_stepper.png",
        tag: "100% On-Device Privacy",
        title: "Custom Limits & Face ID",
        subtitle: "Adjustable 1–9 limit stepper with biometric security"
    )
]

func renderFramedScreenshot(spec: ScreenshotSpec, outPaths: [String]) {
    let width = 1080
    let height = 2160

    let rep = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: width,
        pixelsHigh: height,
        bitsPerSample: 8,
        samplesPerPixel: 4,
        hasAlpha: true,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: width * 4,
        bitsPerPixel: 32
    )!

    NSGraphicsContext.saveGraphicsState()
    guard let ctx = NSGraphicsContext(bitmapImageRep: rep) else { return }
    NSGraphicsContext.current = ctx
    let cg = ctx.cgContext

    // Canvas background #F2F0EA
    let bg = NSColor(red: 242/255.0, green: 240/255.0, blue: 234/255.0, alpha: 1.0)
    bg.setFill()
    NSRect(x: 0, y: 0, width: width, height: height).fill()

    // 1. Tag Pill
    let tagFont = NSFont.systemFont(ofSize: 22, weight: .semibold)
    let tagAttrs: [NSAttributedString.Key: Any] = [
        .font: tagFont,
        .foregroundColor: NSColor(red: 74/255.0, green: 124/255.0, blue: 89/255.0, alpha: 1.0)
    ]
    let tagStr = NSAttributedString(string: spec.tag.uppercased(), attributes: tagAttrs)
    let tagSize = tagStr.size()
    let pillRect = NSRect(x: 80, y: CGFloat(height) - 130, width: tagSize.width + 28, height: 40)
    let pillPath = NSBezierPath(roundedRect: pillRect, xRadius: 20, yRadius: 20)
    NSColor(red: 226/255.0, green: 238/255.0, blue: 228/255.0, alpha: 1.0).setFill()
    pillPath.fill()
    tagStr.draw(at: NSPoint(x: 94, y: CGFloat(height) - 124))

    // 2. Headline
    let titleFont = NSFont.systemFont(ofSize: 58, weight: .bold)
    let titleAttrs: [NSAttributedString.Key: Any] = [
        .font: titleFont,
        .foregroundColor: NSColor(red: 28/255.0, green: 24/255.0, blue: 20/255.0, alpha: 1.0)
    ]
    let titleStr = NSAttributedString(string: spec.title, attributes: titleAttrs)
    titleStr.draw(at: NSPoint(x: 80, y: CGFloat(height) - 215))

    // 3. Subtitle
    let subFont = NSFont.systemFont(ofSize: 28, weight: .medium)
    let subAttrs: [NSAttributedString.Key: Any] = [
        .font: subFont,
        .foregroundColor: NSColor(red: 100/255.0, green: 95/255.0, blue: 88/255.0, alpha: 1.0)
    ]
    let subStr = NSAttributedString(string: spec.subtitle, attributes: subAttrs)
    subStr.draw(at: NSPoint(x: 80, y: CGFloat(height) - 265))

    // 4. Phone Screen Mockup
    let phoneW: CGFloat = 880
    let phoneH: CGFloat = 1760
    let phoneX: CGFloat = (CGFloat(width) - phoneW) / 2.0
    let phoneY: CGFloat = -40.0 // Bleeds off bottom edge

    let cornerRadius: CGFloat = 48.0
    let phoneRect = NSRect(x: phoneX, y: phoneY, width: phoneW, height: phoneH)
    let clipPath = NSBezierPath(roundedRect: phoneRect, xRadius: cornerRadius, yRadius: cornerRadius)

    // Shadow
    cg.saveGState()
    cg.setShadow(offset: CGSize(width: 0, height: -12), blur: 32, color: NSColor(white: 0, alpha: 0.14).cgColor)
    NSColor.black.setFill()
    clipPath.fill()
    cg.restoreGState()

    // Draw Screen Image
    if let srcImg = NSImage(contentsOfFile: spec.srcFile) {
        cg.saveGState()
        clipPath.addClip()
        srcImg.draw(in: phoneRect, from: .zero, operation: .copy, fraction: 1.0)
        cg.restoreGState()
    }

    // Border
    let borderPath = NSBezierPath(roundedRect: phoneRect, xRadius: cornerRadius, yRadius: cornerRadius)
    borderPath.lineWidth = 3.0
    NSColor(red: 220/255.0, green: 216/255.0, blue: 208/255.0, alpha: 1.0).setStroke()
    borderPath.stroke()

    NSGraphicsContext.restoreGraphicsState()

    guard let pngData = rep.representation(using: .png, properties: [:]) else { return }
    for path in outPaths {
        let folder = (path as NSString).deletingLastPathComponent
        try? FileManager.default.createDirectory(atPath: folder, withIntermediateDirectories: true)
        try? pngData.write(to: URL(fileURLWithPath: path))
        print("  ✓ Generated: \(path)")
    }
}

print("🎨 Generating Stuck (com.mtpnda.stuck) Official Play Store & App Store Screenshots...")

let playPhoneDir = "\(rootDir)/android/app/src/main/play/listings/en-US/graphics/phone-screenshots"
let playSevenDir = "\(rootDir)/android/app/src/main/play/listings/en-US/graphics/seven-inch-screenshots"
let playTenDir = "\(rootDir)/android/app/src/main/play/listings/en-US/graphics/ten-inch-screenshots"
let rootPlayPhoneDir = "\(rootDir)/play/listings/en-US/graphics/phone-screenshots"
let assetsStoreDir = "\(rootDir)/assets/store"

for spec in specs {
    let targets = [
        "\(playPhoneDir)/\(spec.filename)",
        "\(playSevenDir)/\(spec.filename)",
        "\(playTenDir)/\(spec.filename)",
        "\(rootPlayPhoneDir)/\(spec.filename)",
        "\(assetsStoreDir)/\(spec.filename)"
    ]
    renderFramedScreenshot(spec: spec, outPaths: targets)
}

print("✨ All 7 Play Store Screenshots Successfully Generated & Mirrored!")
