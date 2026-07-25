
import 'test_spm_platform_interface.dart';

class TestSpm {
  Future<String?> getPlatformVersion() {
    return TestSpmPlatform.instance.getPlatformVersion();
  }
}
