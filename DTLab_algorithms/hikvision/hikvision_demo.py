import cv2

from hikvision.camera_controller import CameraController

controller = CameraController()

# ptz = controller.get_ptz()
# ptz.pan -= 100
# controller.set_ptz(ptz)
while True:
    # 读取摄像头的当前帧
    ret, frame = controller.get_frame()
    # 如果帧读取成功
    if ret:
        # 在窗口中显示帧
        cv2.imshow("TpLink IPC Camera", frame)
    # 按下 'ESC' 退出
    if cv2.waitKey(1) & 0xFF == 27:
        break
