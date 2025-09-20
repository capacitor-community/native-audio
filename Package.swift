// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorCommunityNativeAudio",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorCommunityNativeAudio",
            targets: ["NativeAudio"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "NativeAudio",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NativeAudio"),
        .testTarget(
            name: "NativeAudioTests",
            dependencies: ["NativeAudio"],
            path: "ios/Tests/NativeAudioTests")
    ]
)