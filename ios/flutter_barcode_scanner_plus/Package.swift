// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "flutter_barcode_scanner_plus",
    platforms: [
        .iOS("13.0")
    ],
    products: [
        .library(name: "flutter-barcode-scanner-plus", targets: ["flutter_barcode_scanner_plus"])
    ],
    dependencies: [],
    targets: [
        .target(
            name: "flutter_barcode_scanner_plus",
            dependencies: [],
            path: "../Classes"
        )
    ]
)
