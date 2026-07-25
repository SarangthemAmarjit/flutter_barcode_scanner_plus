import 'package:flutter_test/flutter_test.dart';
import 'package:test_spm/test_spm.dart';
import 'package:test_spm/test_spm_platform_interface.dart';
import 'package:test_spm/test_spm_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockTestSpmPlatform
    with MockPlatformInterfaceMixin
    implements TestSpmPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final TestSpmPlatform initialPlatform = TestSpmPlatform.instance;

  test('$MethodChannelTestSpm is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelTestSpm>());
  });

  test('getPlatformVersion', () async {
    TestSpm testSpmPlugin = TestSpm();
    MockTestSpmPlatform fakePlatform = MockTestSpmPlatform();
    TestSpmPlatform.instance = fakePlatform;

    expect(await testSpmPlugin.getPlatformVersion(), '42');
  });
}
