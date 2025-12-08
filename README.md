# yapboz

Android için WebView tabanlı mini eşleştirme oyunudur. Uygulama, depodaki
`app/src/main/assets/index.html` dosyasını yerel olarak yükleyerek çevrimdışı
çalışır ve depoda ikili (binary) dosya bırakmamak için base64 metninden
her derlemede üretilen uygulama simgesini kullanır.

## Derleme

1. Projeyi Android Studio ile açın.
2. Gerekirse Gradle senkronizasyonunu başlatın ve `assembleDebug` ya da
   `assembleRelease` görevlerini çalıştırın. `preBuild` adımı, `app/icon_base64.txt`
   içindeki veriyi kullanarak gerekli `mipmap` klasörlerine uygulama simgesini
   otomatik olarak üretir.
