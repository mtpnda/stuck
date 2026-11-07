import Foundation

struct Profile: Codable, Equatable {
    var name: String
    var email: String?
    var avatarUrl: String?
    var faceId: Bool = true
    var provider: String = "local" // google, apple, guest, local, preset
    var createdAt: Date = Date()

    var initials: String {
        let parts = name.split(separator: " ").filter { !$0.isEmpty }
        if parts.isEmpty { return "U" }
        if parts.count == 1 { return String(parts[0].prefix(2)).uppercased() }
        return (String(parts[0].prefix(1)) + String(parts[1].prefix(1))).uppercased()
    }
}
