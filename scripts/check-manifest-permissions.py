import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ANDROID = "{http://schemas.android.com/apk/res/android}"
TOOLS = "{http://schemas.android.com/tools}"
ALLOWED_PERMISSIONS = {
    "android.permission.USE_BIOMETRIC",
    "android.permission.VIBRATE",
}
MODULE_PERMISSIONS = {
    "feature-translate": {"android.permission.INTERNET", "android.permission.POST_NOTIFICATIONS"},
}
FORBIDDEN_SERVICE_PERMISSIONS = {
    "android.permission.BIND_ACCESSIBILITY_SERVICE",
    "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
}
ALLOWED_SERVICE_GUARDS = {
    ("ime", ".OtpNotificationService"): {"android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"},
}


def manifests():
    for path in sorted(ROOT.glob("*/src/main/AndroidManifest.xml")):
        yield path


def guard_allowed(module, node, guard):
    key = (module, node.get(ANDROID + "name", ""))
    return node.tag == "service" and guard in ALLOWED_SERVICE_GUARDS.get(key, set())


def check(path, errors):
    name = path.relative_to(ROOT)
    module = path.parts[-4]
    allowed = ALLOWED_PERMISSIONS | MODULE_PERMISSIONS.get(module, set())
    for node in ET.parse(path).getroot().iter():
        value = node.get(ANDROID + "name", "")
        removal = node.get(TOOLS + "node") == "remove"
        if node.tag.startswith("uses-permission") and not removal and value not in allowed:
            errors.append(f"{name}: permission {value} is not on the allowed list")
        guard = node.get(ANDROID + "permission", "")
        if guard in FORBIDDEN_SERVICE_PERMISSIONS and not guard_allowed(module, node, guard):
            errors.append(f"{name}: {guard} is not allowed")
        if node.tag == "application" and node.get(ANDROID + "allowBackup") == "true":
            errors.append(f"{name}: allowBackup must be false")


def main():
    errors = []
    for path in manifests():
        check(path, errors)
    for error in errors:
        print(error)
    print(f"manifest: {len(errors)} problem(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
