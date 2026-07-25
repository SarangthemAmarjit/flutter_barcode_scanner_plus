import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'test_spm_method_channel.dart';

abstract class TestSpmPlatform extends PlatformInterface {
  /// Constructs a TestSpmPlatform.
  TestSpmPlatform() : super(token: _token);

  static final Object _token = Object();

  static TestSpmPlatform _instance = MethodChannelTestSpm();

  /// The default instance of [TestSpmPlatform] to use.
  ///
  /// Defaults to [MethodChannelTestSpm].
  static TestSpmPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [TestSpmPlatform] when
  /// they register themselves.
  static set instance(TestSpmPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
