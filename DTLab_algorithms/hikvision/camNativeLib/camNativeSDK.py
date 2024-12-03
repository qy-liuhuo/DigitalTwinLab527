import os,ctypes
import logging
import subprocess
import cv2
import numpy as np

from hikvision.camNativeLib.NET_DVR_USER_LOGIN_INFO import *
from hikvision.camNativeLib.NET_DVR_DEVICEINFO_V40 import *
from hikvision.camNativeLib.NET_DVR_PTZPOS import *
from hikvision.camNativeLib.camEnum import *
from hikvision.camNativeLib.camPTZ import *
import hikvision.camNativeLib.camPTZ

class HKSdkApi:
    def __init__(self,ip , username, password, sdk_path):
        self.ip = ip
        self.path = sdk_path
        self.username = username
        self.password = password
        strPath = sdk_path.encode("utf-8") + b'/libhcnetsdk.so'
        self.Objdll = ctypes.CDLL(strPath)
        self.dll_list = []
        # 缓冲数据用
        self.frame_data = bytearray()
        self.capture = None
        #登陆设备信息
        self.dvrDeviceInfo=None
        #登陆用户信息
        self.dvrLoginInfo=None
        self.userid=None


# 遍历动态链接库目录
    def add_dll(self):
        files = os.listdir(self.path)
        for file in files:
            if not os.path.isdir(self.path + file):
                if file.endswith(".so"):
                    self.dll_list.append(self.path +"/"+ file)
            else:
                self.add_dll(self.path + file + "/", self.dll_list)

    # 载入动态链接库
    def callCpp(self,func_name, *args):
        for so_lib in self.dll_list:
            try:
                lib = ctypes.cdll.LoadLibrary(so_lib)
                try:
                    value = eval("lib.%s" % func_name)(*args)
                    # print("调用的库：" + so_lib)
                    # print("执行成功,返回值：" + str(value))
                    return value
                except:
                    continue
            except:
                print("库文件载入失败：" + so_lib)
                continue
        print("没有找到接口！")
        return False

    #摄像头用户登陆
    def NET_DVR_Login_V40(self, wDVRPort=8000):
        # set_overtime = self.Objdll.NET_DVR_SetConnectTime(5000, 4)
        set_overtime = self.callCpp("NET_DVR_SetConnectTime", 5000, 4)  # 设置超时
        if set_overtime:
            logging.info(self.ip + ", 设置超时时间成功")
        else:
            error_info = self.callCpp("NET_DVR_GetLastError")
            logging.error(self.ip + ", 设置超时错误信息：" + str(error_info))
            return False

        self.dvrLoginInfo= NET_DVR_USER_LOGIN_INFO()
        # c++传递进去的是byte型数据，需要转成byte型传进去，否则会乱码
        self.dvrLoginInfo.sDeviceAddress = (ctypes.c_byte * 129)(*[ctypes.c_byte(ord(c)) for c in self.ip])
        self.dvrLoginInfo.sUserName = (ctypes.c_byte * 64)(*[ctypes.c_byte(ord(c)) for c in self.username])
        self.dvrLoginInfo.sPassword = (ctypes.c_byte * 64)(*[ctypes.c_byte(ord(c)) for c in self.password])
        self.dvrLoginInfo.wPort = wDVRPort
        self.dvrLoginInfo.bUseAsynLogin=0
        self.dvrDeviceInfo=NET_DVR_DEVICEINFO_V40()
        dvrUserLoginRef=ctypes.byref(self.dvrLoginInfo)
        dvrDeviceInfoRef=ctypes.byref(self.dvrDeviceInfo)
        lUserID = self.callCpp("NET_DVR_Login_V40", dvrUserLoginRef,dvrDeviceInfoRef)
        # print(self.ip + ", 登录结果：" + str(lUserID))
        # print(self.dvrDeviceInfo.byPasswordLevel)
        # dvrDeviceInfoRef.struDeviceV30.byStartChan
        if lUserID == -1:  # -1表示失败，其他值表示返回的用户ID值。
            error_info = self.callCpp("NET_DVR_GetLastError")
            print(self.ip + ", 登录错误信息：" + str(error_info))
        print("登录成功")
        return lUserID

    def NET_DVR_Login_V30(self, wDVRPort=8000):
        # set_overtime = self.callCpp("NET_DVR_SetConnectTime", 5000, 4)  # 设置超时
        set_overtime = self.Objdll.NET_DVR_SetConnectTime(5000, 4)
        if set_overtime:
            logging.info(self.ip + ", 设置超时时间成功")
        else:
            error_info = self.callCpp("NET_DVR_GetLastError")
            logging.error(self.ip + ", 设置超时错误信息：" + str(error_info))
            return False

        # c++传递进去的是byte型数据，需要转成byte型传进去，否则会乱码
        sDVRIP_bytes = bytes(self.ip, "ascii")
        sUserName = bytes(self.username, "ascii")
        sPassword = bytes(self.password, "ascii")
        DeviceInfo = LPNET_DVR_DEVICEINFO_V30()
        DeviceInfoRef = ctypes.byref(DeviceInfo)
        # lUserID = self.callCpp("NET_DVR_Login_V30", sDVRIP_bytes, wDVRPort, sUserName, sPassword, DeviceInfoRef)
        lUserID = self.Objdll.NET_DVR_Login_V30(sDVRIP_bytes, wDVRPort, sUserName, sPassword,
                                      ctypes.byref(DeviceInfo))
        logging.info(self.ip + ", 登录结果：" + str(lUserID))
        if lUserID == -1:  # -1表示失败，其他值表示返回的用户ID值。
            error_info = self.callCpp("NET_DVR_GetLastError")
            logging.error(self.ip + ", 登录错误信息：" + str(error_info))
        self.userid=lUserID
        return lUserID
    
    # 登出摄像头
    def NET_DVR_Logout(self):
        res=self.callCpp("NET_DVR_Logout",self.userid)
        if res == -1:  # -1表示失败，其他值表示返回的用户ID值。
            error_info = self.callCpp("NET_DVR_GetLastError")
            logging.error(self.userid+", 登出错误信息：" + str(error_info))
        return res

    def NET_DVR_GetDVRConfig(self,lUserID,dwCommand,IChannel,IpOutBuffer,IpBytesReturned):       
        IpOutBufferRef=ctypes.byref(IpOutBuffer)
        buffSize=ctypes.sizeof(IpOutBuffer)
        IpBytesReturnedRef=ctypes.byref(IpBytesReturned)
        res=self.callCpp("NET_DVR_GetDVRConfig",lUserID,dwCommand,IChannel,IpOutBufferRef,buffSize,IpBytesReturnedRef)
        if res == -1:  # -1表示失败
            error_info = self.callCpp("NET_DVR_GetLastError")
            logging.error(dwCommand+", 接口调用错误信息：" + str(error_info))
        return res

    #计算16进制值转10进制值
    def dex2hex(self,hexValue):
        return int(hex(hexValue)[2:].upper()) // 10

    def hex2dec(self,decValue):
        return int(str(decValue * 10), 16)

    def control(self,ptzCommand,ptzTrigger,speed):
        res=self.callCpp("NET_DVR_PTZControlWithSpeed_Other",self.userid,self.dvrDeviceInfo.struDeviceV30.byStartChan,ptzCommand,ptzTrigger,speed)
        if res == -1:  # -1表示失败
           error_info = self.callCpp("NET_DVR_GetLastError")
           logging.error(ptzCommand+"控制云台PTZ错误信息：" + str(error_info))           
        return res

    #获取摄像头PTZ
    def getPTZ(self):
        ptzpos=NET_DVR_PTZPOS()
        resBuff=ctypes.c_buffer(255)
        res=self.NET_DVR_GetDVRConfig(self.userid,NET_DVR_GET_PTZPOS,self.dvrDeviceInfo.struDeviceV30.byStartChan,ptzpos,resBuff)
        if res == -1:  # -1表示失败
           error_info = self.callCpp("NET_DVR_GetLastError")
           logging.error("获取PTZ错误信息：" + str(error_info))
           return res
        # print(self.dvrDeviceInfo.struDeviceV30.byStartChan)
        # print(ptzpos.wPanPos)
        # print(ptzpos.wPanPos)
        ptz=CamPTZ()
        ptz.action=self.dex2hex(ptzpos.wAction)
        ptz.pan=self.dex2hex(ptzpos.wPanPos)
        ptz.tilt=self.dex2hex(ptzpos.wTiltPos)
        ptz.zoom=self.dex2hex(ptzpos.wZoomPos)
        return ptz

    # 云台控制操作
    def NET_DVR_PTZControl_Other(self,lUserID, lChannel, dwPTZCommand, dwStop):
        res = self.callCpp("NET_DVR_PTZControl_Other", lUserID, lChannel, dwPTZCommand, dwStop)
        if res:
            print("控制成功")
        else:
            print("控制失败: " + str(self.callCpp("NET_DVR_GetLastError")))

    def NET_DVR_Init(self):
        res=self.callCpp("NET_DVR_Init")
        if not res:
            print("Fail"+str(self.callCpp("NET_DVR_GetLastError")))
        return res



    def setPTZ(self,ptz):
        ptzpos = NET_DVR_PTZPOS()
        ptzpos.wAction=1
        ptzpos.wPanPos=self.hex2dec(ptz.pan)
        ptzpos.wTiltPos=self.hex2dec(ptz.tilt)
        ptzpos.wZoomPos=self.hex2dec(ptz.zoom)
        res=self.callCpp("NET_DVR_SetDVRConfig",self.userid, NET_DVR_SET_PTZPOS, self.dvrDeviceInfo.struDeviceV30.byStartChan, ctypes.byref(ptzpos), ctypes.sizeof(ptzpos))
        if res == -1:
            error_info = self.callCpp("NET_DVR_GetLastError")
            logging.error("设置PTZ错误信息：" + str(error_info))
        return res
