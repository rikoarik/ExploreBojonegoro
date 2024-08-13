# Sistem Informasi Geografis Objek Wisata Bojonegoro

Aplikasi ini merupakan aplikasi sistem informasi geografis yang dirancang untuk menampilkan informasi mengenai objek wisata yang ada di Kabupaten Bojonegoro. Aplikasi ini memanfaatkan API Weather untuk memberikan informasi cuaca terkini dan Mapbox untuk menampilkan peta interaktif.

## Fitur

- **Peta Interaktif**: Menampilkan lokasi objek wisata di peta menggunakan Mapbox.
- **Informasi Cuaca**: Menampilkan informasi cuaca terkini untuk lokasi objek wisata menggunakan API Weather.
- **Detail Objek Wisata**: Menampilkan informasi detail mengenai objek wisata termasuk nama, alamat, deskripsi, dan gambar.

## Teknologi yang Digunakan

- **Android Studio**: IDE untuk pengembangan aplikasi Android.
- **Kotlin**: Bahasa pemrograman utama untuk pengembangan aplikasi.
- **Mapbox**: Untuk menampilkan peta interaktif.
- **API Weather**: Untuk mendapatkan informasi cuaca terkini.

## Prasyarat

- Android Studio
- Akun Mapbox untuk mendapatkan akses token
- Akun untuk API Weather

## Instalasi

1. Clone repositori ini:

    ```bash
      https://github.com/rikoarik/ExploreBojonegoro.git
    ```

2. Buka proyek di Android Studio.

3. Tambahkan token Mapbox Anda ke file `local.properties`:

    ```
    MAPBOX_ACCESS_TOKEN=your_mapbox_access_token
    ```

5. Sinkronkan proyek dengan Gradle.

## Konfigurasi

1. **Mapbox**: 
    - Tambahkan dependensi Mapbox di file `build.gradle` (app level):

    ```groovy
    implementation 'com.mapbox.maps:maps-sdk:10.0.0'
    ```

2. **API Weather**:
    - Integrasikan API Weather ke dalam aplikasi menggunakan library HTTP seperti Retrofit.

## Penggunaan

- Jalankan aplikasi di emulator atau perangkat Android.
- Aplikasi akan menampilkan peta interaktif dengan lokasi objek wisata.
- Ketuk lokasi objek wisata untuk melihat detail dan cuaca terkini.

## Kontribusi

Jika Anda ingin berkontribusi pada proyek ini, silakan buat pull request atau buka issue jika Anda menemukan bug atau memiliki saran.

## Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

## Design Figma

-Prototype
    ```bash
    https://www.figma.com/proto/sZXFKixzOjrGgF579ye2Ak/Explore-Bojonegoro?node-id=2-4&t=jIhdI95ZtY5U9MO5-1
    ```
-UI UX
    ```bash
    https://www.figma.com/design/sZXFKixzOjrGgF579ye2Ak/Explore-Bojonegoro?node-id=2-3&t=jIhdI95ZtY5U9MO5-1
    ```

## Kontak

Jika Anda memiliki pertanyaan atau membutuhkan bantuan, silakan hubungi saya di [rikoarik04@gmail.com](mailto:rikoarik04@gmail.com).

---

Terima kasih telah menggunakan aplikasi Sistem Informasi Geografis Objek Wisata Bojonegoro!
