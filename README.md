# Demo webcam LAN - Java TCP/UDP

## Chay tren Windows

Mo PowerShell tai thu muc chua file nay. Can JDK 17+ (may hien tai: JDK 21).

```powershell
.\build.ps1
```

Mo 3 terminal cung thu muc, chay theo thu tu:

```powershell
# Terminal 1
.\run.ps1 server
# Terminal 2: webcam mac dinh, tu dung sau 60 giay
.\run.ps1 sender
# Terminal 3: cua so Swing xem video
.\run.ps1 viewer
```

Neu camera khong mo duoc, dong ung dung dang dung webcam va kiem tra quyen
camera cua Windows. Neu ban biet camera nam o chi so khac, truyen
`-CameraIndex 1` (hoac chi so dung). Khong co camera thi dung:

```powershell
.\run.ps1 test-pattern -Seconds 120
.\run.ps1 viewer
```

Chay may xem tren LAN: ` .\run.ps1 viewer -ServerAddress 192.168.1.10 `,
thay bang IP LAN that cua server. Cho phep cong TCP 5000 va UDP 5001 trong
firewall neu can. Khong dung localhost de ket noi may khac.

Mo them viewer de xem dong thoi. Dong cua so de thoat viewer.
Sender tu dung sau so giay da chon; `-Seconds` nhan 1..3600.
Khi sender dung, viewer giu anh cu va hien trang thai khong co khung moi.
Muon xem stream moi, dong va mo lai viewer sau khi sender moi da chay.

## Thu vien

- `lib/opencv-500.jar` va `native/opencv_java500.dll` da duoc sao chep tu
  ban OpenCV san co trong Downloads/opencv, cung phien ban 5.0.0, x64.
- Chi sender webcam can native OpenCV. Viewer dung Java ImageIO va Swing.
- `build.ps1` co chu y khong bien dich `CommandHandler.java`: file JSON cu
  khong duoc dung trong giao thuc van ban hien tai. Khong xoa file cu.
- Script uu tien JDK 21 da tim thay tren may, neu khong co thi dung PATH.

## Cac file moi

- `DemoConnection.java`: HELLO, dang ky UDP, heartbeat TCP moi 10 giay.
- `VideoFrames.java`: chia/ghep JPEG; gioi han khung, timeout, bo goi trung.
- `WebcamSender.java`: webcam 320x240, JPEG quality 60; hoac anh gia lap.
- `WebcamViewer.java`: ghep/kiem tra JPEG, Swing chi hien anh moi nhat.
- `TestVideoFrames.java`: kiem thu cac truong hop dao thu tu/trung/thieu/loi.

## Giao thuc demo

Giu TCP length-prefix 4 byte + UTF-8 va cac lenh da co. HELLO tra ma phien
va token. UDP REGISTER gan IP/cong voi phien. Khong in token vao log.

Moi khung JPEG chia thanh cac manh toi da 240 byte, ma hoa Base64:

```
Nguon -> server: DATA <sessionId> <streamId> <frameId>:<index>:<total>:<base64>
Server -> xem:   DATA <streamId> <frameId>:<index>:<total>:<base64>
```

- index bat dau 0; frameId tang tu 0 trong moi stream; giai ma theo Stream ID.
- Toi da 512 byte/datagram; 128 KiB/frame; toi da 547 manh/frame.
- JPEG 320x240. Viewer kiem tra kich thuoc truoc khi giai nen.
- Chi giu 1 frame dang ghep, toi da 500 ms tinh tu manh dau tien nhan duoc.
- Frame moi thay frame cu chua du; manh trung va frame da hoan thanh bi bo.
- Khong gui lai manh video; dang ky UDP duoc thu toi da 3 lan.
- Thong ke INCOMPLETE_DROPPED chi dem frame da bat dau ghep nhung bi bo;
  khong the dem frame mat toan bo goi tu thong ke nay.

Day la giao thuc demo Base64, chua phai header nhi phan cuoi cung cua nhom.
Can thong nhat voi nguoi 2 truoc khi tich hop client chinh thuc.

## Gioi han

Chua co Docker, hang doi relay co gioi han, gioi han so client, tu dang ky lai
stream moi hay thong bao dung stream chu dong. Token chua ma hoa va dang ky
UDP chua co challenge-response chong gia mao. Demo LAN, khong public Internet.
FPS thuc te phu thuoc camera, nen JPEG va so manh; khong cam ket 10 FPS.
TCP read timeout la timeout tung lan doc, chua phai han chot cho ca thong diep.

## Ket qua kiem thu thuc te 01/11/2026

- Bien dich voi JDK 21 va OpenCV 5.0.0: thanh cong.
- TestVideoFrames: PASS dao thu tu, trung, thieu, qua han, Base64/so manh loi.
- TestRelayClient tren loopback: KIEM TRA THANH CONG.
- Nguon anh gia lap 8 giay: gui 45 frame.
- Hai viewer headless dong thoi: moi viewer giai ma 10 frame 320x240,
  INCOMPLETE_DROPPED=0, INVALID_IMAGES=0 trong luot thu nay.
- Webcam that index 0: KHONG MO DUOC, OpenCV bao Camera index out of range.
  Chua xac nhan duoc hinh anh webcam that, GUI Swing hoac truyen giua hai may LAN.
- Server kiem thu dung cong rieng 24500/24501 va da dung sau khi thu.

## AI log de nhom ra soat

01/11/2026 - Codex bo sung client demo webcam, chia/ghep anh, script build/run,
va kiem thu; cac file moi duoc liet ke o tren. Ket qua tu dong ghi o muc kiem
thu. Thanh vien tu dien phan giu/sua/loai bo va cach tu minh kiem chung.
