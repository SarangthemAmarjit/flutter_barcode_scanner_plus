import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'test_spm_platform_interface.dart';

/// An implementation of [TestSpmPlatform] that uses method channels.
class MethodChannelTestSpm extends TestSpmPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('test_spm');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }
}
