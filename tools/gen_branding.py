#!/usr/bin/env python3
"""
gen_branding.py
Generates the official "Stuck" (com.mtpnda.stuck) branding assets:
- App Icon: "Stack of Bars" representing cognitive load layers
- BioCue DLS Palette: Charcoal (#1C1814), Warm Oat (#F2F0EA), Crisp Oat (#FCFBF8), Calm Sage (#4A7C59), Alert Coral (#C84B31)
- Outputs:
    - assets/app-icon.png (1024x1024)
    - assets/icon.svg
    - assets/feature-graphic.png (1024x500)
    - android/app/src/main/res/mipmap-*/ic_launcher.png & ic_launcher_round.png
    - play/listings/en-US/graphics/icon/icon.png (512x512)
    - play/listings/en-US/graphics/feature-graphic/feature-graphic.png (1024x500)
    - ios/Contexts/Assets.xcassets/AppIcon.appiconset/
"""

import os
import sys
import zlib
import struct
import math
import subprocess

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# BioCue DLS Color Constants
CHARCOAL = (28, 24, 20, 255)       # #1C1814
WARM_OAT = (242, 240, 234, 255)    # #F2F0EA
CRISP_OAT = (252, 251, 248, 255)   # #FCFBF8
CALM_SAGE = (74, 124, 89, 255)     # #4A7C59
ALERT_CORAL = (200, 75, 49, 255)   # #C84B31
MUTED_INK = (122, 114, 102, 255)   # #7A7266
BORDER_COL = (229, 224, 216, 255)  # #E5E0D8

def save_png(filename, width, height, pixels):
    os.makedirs(os.path.dirname(os.path.abspath(filename)), exist_ok=True)
    raw_data = bytearray()
    for y in range(height):
        raw_data.append(0) # Filter type 0
        for x in range(width):
            raw_data.extend(pixels[y * width + x])
    
    def chunk(chunk_type, data):
        return struct.pack('>I', len(data)) + chunk_type + data + struct.pack('>I', zlib.crc32(chunk_type + data) & 0xffffffff)

    header = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    idat = zlib.compress(bytes(raw_data), 9)
    png_data = header + chunk(b'IHDR', ihdr) + chunk(b'IDAT', idat) + chunk(b'IEND', b'')
    with open(filename, 'wb') as f:
        f.write(png_data)

def draw_rounded_rect(pixels, w, h, rx, ry, rw, rh, radius, color):
    r_color = color[:3]
    a_color = color[3] if len(color) > 3 else 255
    for y in range(max(0, ry), min(h, ry + rh)):
        for x in range(max(0, rx), min(w, rx + rw)):
            dx = 0
            if x < rx + radius:
                dx = rx + radius - x
            elif x >= rx + rw - radius:
                dx = x - (rx + rw - radius - 1)
            
            dy = 0
            if y < ry + radius:
                dy = ry + radius - y
            elif y >= ry + rh - radius:
                dy = y - (ry + rh - radius - 1)
            
            orig = pixels[y * w + x]
            if dx > 0 and dy > 0:
                dist = math.sqrt(dx * dx + dy * dy)
                if dist > radius:
                    continue
                elif dist > radius - 1.0:
                    aa = 1.0 - (dist - (radius - 1.0))
                    alpha = (a_color / 255.0) * aa
                    r = int(r_color[0] * alpha + orig[0] * (1.0 - alpha))
                    g = int(r_color[1] * alpha + orig[1] * (1.0 - alpha))
                    b = int(r_color[2] * alpha + orig[2] * (1.0 - alpha))
                    pixels[y * w + x] = (r, g, b, 255)
                    continue
            
            alpha = a_color / 255.0
            r = int(r_color[0] * alpha + orig[0] * (1.0 - alpha))
            g = int(r_color[1] * alpha + orig[1] * (1.0 - alpha))
            b = int(r_color[2] * alpha + orig[2] * (1.0 - alpha))
            pixels[y * w + x] = (r, g, b, 255)

def draw_circle(pixels, w, h, cx, cy, radius, color):
    r_color = color[:3]
    a_color = color[3] if len(color) > 3 else 255
    r_int = int(radius + 1)
    for y in range(max(0, cy - r_int), min(h, cy + r_int + 1)):
        for x in range(max(0, cx - r_int), min(w, cx + r_int + 1)):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx * dx + dy * dy)
            if dist > radius:
                continue
            orig = pixels[y * w + x]
            if dist > radius - 1.0:
                aa = 1.0 - (dist - (radius - 1.0))
                alpha = (a_color / 255.0) * aa
                r = int(r_color[0] * alpha + orig[0] * (1.0 - alpha))
                g = int(r_color[1] * alpha + orig[1] * (1.0 - alpha))
                b = int(r_color[2] * alpha + orig[2] * (1.0 - alpha))
                pixels[y * w + x] = (r, g, b, 255)
            else:
                alpha = a_color / 255.0
                r = int(r_color[0] * alpha + orig[0] * (1.0 - alpha))
                g = int(r_color[1] * alpha + orig[1] * (1.0 - alpha))
                b = int(r_color[2] * alpha + orig[2] * (1.0 - alpha))
                pixels[y * w + x] = (r, g, b, 255)

def generate_app_icon_1024():
    """Generates 1024x1024 master icon with Stack of Bars on BioCue Charcoal."""
    W = H = 1024
    # Background: Warm Oat canvas with full bleed
    pixels = [WARM_OAT for _ in range(W * H)]
    
    # Charcoal Container Card (Continuous curvature rounded squircle)
    card_margin = 84
    card_size = W - 2 * card_margin
    card_rad = 220
    
    # Subtle card shadow
    for s in range(16, 0, -2):
        s_alpha = int(8 * (1.0 - s / 16.0))
        draw_rounded_rect(pixels, W, H, card_margin - s//2, card_margin + s, card_size + s, card_size, card_rad + s//2, (0, 0, 0, s_alpha))
    
    # Main Charcoal Card
    draw_rounded_rect(pixels, W, H, card_margin, card_margin, card_size, card_size, card_rad, CHARCOAL)

    # Inside the Charcoal card: Stack of 4 horizontal rounded bars representing cognitive levels
    # Bar geometry
    bar_h = 76
    bar_radius = bar_h // 2
    gap = 48
    
    # 4 Stacked Bars (Centered horizontally)
    bars = [
        # (width, color, y_offset)
        (380, CALM_SAGE,   "Top: Focus / Active (Calm Sage)"),
        (600, CRISP_OAT,   "Level 2: Active Context (Crisp Oat)"),
        (480, WARM_OAT,    "Level 3: Parked Thought (Warm Oat)"),
        (260, ALERT_CORAL, "Level 4: Cognitive Limit Indicator (Alert Coral)")
    ]
    
    total_stack_h = len(bars) * bar_h + (len(bars) - 1) * gap
    start_y = (H - total_stack_h) // 2
    
    for i, (bar_w, bar_color, _) in enumerate(bars):
        bar_x = (W - bar_w) // 2
        bar_y = start_y + i * (bar_h + gap)
        draw_rounded_rect(pixels, W, H, bar_x, bar_y, bar_w, bar_h, bar_radius, bar_color)
    
    return pixels

def generate_app_icon_round_1024():
    """Generates 1024x1024 round icon for Android adaptive / round launchers."""
    W = H = 1024
    pixels = [(0, 0, 0, 0) for _ in range(W * H)]
    
    # Draw circular Charcoal base
    draw_circle(pixels, W, H, W // 2, H // 2, 490, CHARCOAL)
    
    bar_h = 76
    bar_radius = bar_h // 2
    gap = 48
    bars = [
        (340, CALM_SAGE),
        (560, CRISP_OAT),
        (440, WARM_OAT),
        (240, ALERT_CORAL)
    ]
    total_stack_h = len(bars) * bar_h + (len(bars) - 1) * gap
    start_y = (H - total_stack_h) // 2
    
    for i, (bar_w, bar_color) in enumerate(bars):
        bar_x = (W - bar_w) // 2
        bar_y = start_y + i * (bar_h + gap)
        draw_rounded_rect(pixels, W, H, bar_x, bar_y, bar_w, bar_h, bar_radius, bar_color)
    
    return pixels

def generate_feature_graphic_1024x500():
    """Generates Google Play 1024x500 Feature Graphic with BioCue Warm Oat canvas and Stacked Bars."""
    W = 1024
    H = 500
    pixels = [WARM_OAT for _ in range(W * H)]
    
    # Decorative subtle background grid/lines
    for x in range(0, W, 80):
        for y in range(H):
            pixels[y * W + x] = (235, 230, 222, 255)
    for y in range(0, H, 80):
        for x in range(W):
            pixels[y * W + x] = (235, 230, 222, 255)

    # Center Stack of Bars Badge
    badge_w = 260
    badge_h = 260
    badge_rad = 60
    badge_x = 120
    badge_y = (H - badge_h) // 2
    
    # Badge shadow
    draw_rounded_rect(pixels, W, H, badge_x, badge_y + 12, badge_w, badge_h, badge_rad, (0, 0, 0, 25))
    draw_rounded_rect(pixels, W, H, badge_x, badge_y, badge_w, badge_h, badge_rad, CHARCOAL)
    
    # Inside badge: mini stack of bars
    mb_h = 22
    mb_gap = 14
    mb_rad = mb_h // 2
    m_bars = [
        (100, CALM_SAGE),
        (170, CRISP_OAT),
        (140, WARM_OAT),
        (75, ALERT_CORAL)
    ]
    total_mb_h = len(m_bars) * mb_h + (len(m_bars) - 1) * mb_gap
    mb_start_y = badge_y + (badge_h - total_mb_h) // 2
    for i, (mw, mc) in enumerate(m_bars):
        mx = badge_x + (badge_w - mw) // 2
        my = mb_start_y + i * (mb_h + mb_gap)
        draw_rounded_rect(pixels, W, H, mx, my, mw, mb_h, mb_rad, mc)
        
    # Right side decorative balance bars
    right_x = 450
    right_y = 140
    draw_rounded_rect(pixels, W, H, right_x, right_y, 450, 48, 24, CHARCOAL)
    draw_rounded_rect(pixels, W, H, right_x, right_y + 68, 380, 48, 24, CALM_SAGE)
    draw_rounded_rect(pixels, W, H, right_x, right_y + 136, 420, 48, 24, MUTED_INK)
    draw_rounded_rect(pixels, W, H, right_x, right_y + 204, 200, 48, 24, ALERT_CORAL)

    return pixels

def generate_svg():
    svg_content = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="1024" height="1024">
  <defs>
    <filter id="softShadow" x="-10%" y="-10%" width="120%" height="120%">
      <feDropShadow dx="0" dy="16" stdDeviation="24" flood-color="#000000" flood-opacity="0.15" />
    </filter>
  </defs>
  <!-- Warm Oat Canvas -->
  <rect width="1024" height="1024" fill="#F2F0EA" />
  
  <!-- BioCue Charcoal Container -->
  <rect x="84" y="84" width="856" height="856" rx="220" fill="#1C1814" filter="url(#softShadow)" />
  
  <!-- Stack of Bars (Cognitive Load Architecture) -->
  <!-- Bar 1: Calm Sage (Focused) -->
  <rect x="322" y="328" width="380" height="76" rx="38" fill="#4A7C59" />
  <!-- Bar 2: Crisp Oat (Active Context) -->
  <rect x="212" y="452" width="600" height="76" rx="38" fill="#FCFBF8" />
  <!-- Bar 3: Warm Oat (Parked Context) -->
  <rect x="272" y="576" width="480" height="76" rx="38" fill="#F2F0EA" />
  <!-- Bar 4: Alert Coral (Cognitive Limit Alert) -->
  <rect x="382" y="700" width="260" height="76" rx="38" fill="#C84B31" />
</svg>
"""
    return svg_content

def resize_image(src, dst, size):
    os.makedirs(os.path.dirname(os.path.abspath(dst)), exist_ok=True)
    subprocess.run(["sips", "-z", str(size), str(size), src, "--out", dst], check=True, stdout=subprocess.DEVNULL)

def main():
    print("🎨 Generating Stuck (com.mtpnda.stuck) BioCue DLS Branding Assets...")
    
    # 1. Master Icon 1024x1024
    master_icon_path = os.path.join(ROOT_DIR, "assets", "app-icon.png")
    master_pixels = generate_app_icon_1024()
    save_png(master_icon_path, 1024, 1024, master_pixels)
    print(f"  ✓ Master Icon: {master_icon_path} (1024x1024)")
    
    # 2. Master Round Icon 1024x1024
    round_icon_path = os.path.join(ROOT_DIR, "assets", "app-icon-round.png")
    round_pixels = generate_app_icon_round_1024()
    save_png(round_icon_path, 1024, 1024, round_pixels)
    print(f"  ✓ Round Icon: {round_icon_path} (1024x1024)")

    # 3. Vector SVG
    svg_path = os.path.join(ROOT_DIR, "assets", "icon.svg")
    with open(svg_path, "w") as f:
        f.write(generate_svg())
    print(f"  ✓ Vector SVG: {svg_path}")

    # 4. Feature Graphic 1024x500
    feature_path = os.path.join(ROOT_DIR, "assets", "feature-graphic.png")
    feature_pixels = generate_feature_graphic_1024x500()
    save_png(feature_path, 1024, 500, feature_pixels)
    print(f"  ✓ Feature Graphic: {feature_path} (1024x500)")

    # 5. Play Store Listings Assets
    play_icon_dst = os.path.join(ROOT_DIR, "play", "listings", "en-US", "graphics", "icon", "icon.png")
    resize_image(master_icon_path, play_icon_dst, 512)
    print(f"  ✓ Play Store 512x512 Icon: {play_icon_dst}")

    play_feat_dst = os.path.join(ROOT_DIR, "play", "listings", "en-US", "graphics", "feature-graphic", "feature-graphic.png")
    os.makedirs(os.path.dirname(play_feat_dst), exist_ok=True)
    with open(feature_path, "rb") as sf, open(play_feat_dst, "wb") as df:
        df.write(sf.read())
    print(f"  ✓ Play Store Feature Graphic: {play_feat_dst}")

    # Also sync into android/app/src/main/play/
    android_play_dir = os.path.join(ROOT_DIR, "android", "app", "src", "main", "play")
    os.makedirs(android_play_dir, exist_ok=True)
    subprocess.run(["cp", "-R", os.path.join(ROOT_DIR, "play", "."), android_play_dir], check=True)
    print(f"  ✓ Mirrored to Android Play metadata: {android_play_dir}")

    # 6. Android Mipmap Icons
    mipmap_targets = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }
    for folder, sz in mipmap_targets.items():
        # Square
        sq_dst = os.path.join(ROOT_DIR, "android", "app", "src", "main", "res", folder, "ic_launcher.png")
        resize_image(master_icon_path, sq_dst, sz)
        # Round
        rd_dst = os.path.join(ROOT_DIR, "android", "app", "src", "main", "res", folder, "ic_launcher_round.png")
        resize_image(round_icon_path, rd_dst, sz)
    print(f"  ✓ Android Mipmap Launcher & Round Icons (mdpi -> xxxhdpi)")

    # 7. iOS AppIcon Catalog
    ios_icon_dir = os.path.join(ROOT_DIR, "ios", "Contexts", "Assets.xcassets", "AppIcon.appiconset")
    os.makedirs(ios_icon_dir, exist_ok=True)
    resize_image(master_icon_path, os.path.join(ios_icon_dir, "AppIcon-1024.png"), 1024)
    # Write Contents.json
    contents_json = """{
  "images" : [
    {
      "filename" : "AppIcon-1024.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
"""
    with open(os.path.join(ios_icon_dir, "Contents.json"), "w") as f:
        f.write(contents_json)
    print(f"  ✓ iOS Assets.xcassets AppIcon: {ios_icon_dir}")

    # 8. Webhost Assets
    webhost_dir = "/Users/valdiearsanur/Workspace/macbook-air-webhost/apps/stuck/assets"
    try:
        os.makedirs(webhost_dir, exist_ok=True)
        resize_image(master_icon_path, os.path.join(webhost_dir, "app-icon.png"), 512)
        print(f"  ✓ Webhost Portal Asset: {webhost_dir}/app-icon.png")
    except Exception as e:
        print(f"  ⚠ Note on Webhost: {e}")

    print("✨ Stuck Branding Generation Complete!")

if __name__ == "__main__":
    main()
