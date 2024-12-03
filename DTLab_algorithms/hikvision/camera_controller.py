import os
import time

import cv2
from dotenv import load_dotenv

from hikvision.camNativeLib.camEnum import *
from hikvision.camNativeLib.camNativeSDK import HKSdkApi


class CameraController:
    def __init__(self):
        load_dotenv()
        self.camera_ip = os.getenv("CAMERA_IP")
        self.username = os.getenv("CAMERA_USERNAME")
        self.password = os.getenv("CAMERA_PASSWORD")
        self.platform = os.getenv("PLATFORM")
        self.sdk_path = "./hikvision_sdk/" + self.platform
        self.camera_sdk = HKSdkApi(self.camera_ip, self.username, self.password, self.sdk_path)
        self.camera_sdk.add_dll()
        self.camera_sdk.NET_DVR_Init()
        self.camera_sdk.NET_DVR_Login_V40()
        self.cap = cv2.VideoCapture(f'rtsp://{self.username}:{self.password}@{self.camera_ip}/Streaming/Channels/101')

    def get_frame(self):
        return self.cap.read()

    def get_ptz(self):
        return self.camera_sdk.getPTZ()

    def set_ptz(self, ptz):
        self.camera_sdk.setPTZ(ptz)

    def turn_up(self):
        self.camera_sdk.control(TILT_UP, PTZ_CONTROL_START, 7)
        time.sleep(1)
        self.camera_sdk.control(TILT_UP, PTZ_CONTROL_STOP, 7)

    def turn_down(self):
        self.camera_sdk.control(TILT_DOWN, PTZ_CONTROL_START, 7)
        time.sleep(1)
        self.camera_sdk.control(TILT_DOWN, PTZ_CONTROL_STOP, 7)

    def turn_left(self):
        self.camera_sdk.control(PAN_LEFT, PTZ_CONTROL_START, 7)
        time.sleep(1)
        self.camera_sdk.control(PAN_LEFT, PTZ_CONTROL_STOP, 7)

    def turn_right(self):
        self.camera_sdk.control(PAN_RIGHT, PTZ_CONTROL_START, 7)
        time.sleep(1)
        self.camera_sdk.control(PAN_RIGHT, PTZ_CONTROL_STOP, 7)

    def __del__(self):
        self.camera_sdk.NET_DVR_Logout()
        self.cap.release()
        print("CameraController object deleted")
