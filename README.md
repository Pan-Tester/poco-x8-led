# POCO X8 LED
Tak.

O co chodzi? Bawiłem się moim nowym POCO X8 Pro i dowiedziałem się, że ma on podświetlenie RGB (z tyłu obok kamery) i chciałem sobie to skustomizować, ale ustawienia telefonu nie dają innych opcji niż: miganie podczas powiadomień/telefonu i rytmicznego RGB do muzyki.

Dlatego zdekompilowałem parę plików wyciągniętych przez `adb` z telefonu i znalazłem ten piękny kod.

W miui-services.jar (dokładnie w HyperLightsService) jest na sztywno wpisana nazwa aparatu:

`private static final String CAMERA_PKG = "com.android.camera";`

A tu ich zaawansowane sprawdzanie uprawnień:

```java
public boolean checkCustomLightCaller(String pkg) {
    if (pkg != null) {
        return "com.android.camera".equals(pkg) || VOICE_ASSISTANT_PKG.equals(pkg);
    }
    Slog.e("HyperLightsService", "callingPackage is invalid!");
    return false;
}
```
I przez to używając:
`adb service call miui.lights.ILightsManager 4 i32 -16711936 i32 1 i32 2000 i32 500 i32 100 s16 "com.android.camera" i32 12 i32 0`
Możemy wywołać ledy :D

W tym repozytorium znajdziesz parę przykładowych aplikacji działających na podstawie Shizuku i dzięki temu można tego używać bez roota.

Tak, tak, te przykładowe aplikacje są pisane w 80% przez AI (niestety moje umiejętności pisania aplikacji na Androida kończą się na odpaleniu Android IDE).


(ciekawe czy ktos odkryl to przez mną)
> Xiaomi, nie pozywajcie mnie! plz
