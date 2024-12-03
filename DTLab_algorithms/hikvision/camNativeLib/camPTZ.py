class CamPTZ:
    def __init__(self) -> None:
        self.action=None
        self.pan=None
        self.tilt=None
        self.zoom=None

    def __str__(self) -> str:
        return f"action:{self.action}, pan:{self.pan}, tilt:{self.tilt}, zoom:{self.zoom}"