from datetime import datetime
from zoneinfo import ZoneInfo
import requests
import time

HEALTH_CHECK_URL = "헬스 체크 하려는 URL"
WEBHOOK_URL = "디스코드 웹훅 주소"
WEBHOOK_TIMEOUT = 10

CONSECUTIVE_FAILURES = 0
ALERT_THRESHOLD = 5
SLEEP_TIME = 60

DATETIME_FORMAT = '%Y-%m-%d %H:%M:%S'

def sendDiscordNotification(message):
    payload = {
        "content": message
    }
    try:
        response = requests.post(WEBHOOK_URL, json=payload)
        response.raise_for_status()
    except requests.exceptions.RequestException as e:
        print(f"알림 전송에 실패했습니다: {e}")

def checkHealth():
    global CONSECUTIVE_FAILURES

    currentTime = datetime.now(ZoneInfo("Asia/Seoul")).strftime(DATETIME_FORMAT)
    print(f"[{currentTime}] 헬스 체크 시작 (연속 실패 횟수 : {CONSECUTIVE_FAILURES}회)")

    try:
        response = requests.get(HEALTH_CHECK_URL, timeout=WEBHOOK_TIMEOUT)

        if response.status_code == 200:
            print(f"[{currentTime}] ✅ 성공 (상태 코드: {response.status_code})")
            if CONSECUTIVE_FAILURES > 0:
                print("-> 서비스가 정상화되어 연속 실패 횟수를 초기화합니다.")
            CONSECUTIVE_FAILURES = 0
        else:
            handleFailure(f"상태 코드 {response.status_code}")

    except requests.exceptions.RequestException as e:
        handleFailure(f"요청 실패: {e}")

def handleFailure(reason):
    global CONSECUTIVE_FAILURES
    CONSECUTIVE_FAILURES += 1
    print(f"-> ❌ 실패 (사유: {reason}). 연속 실패 횟수: {CONSECUTIVE_FAILURES}회")

    # 연속 실패 횟수가 임계값을 초과하면 알림 전송
    if CONSECUTIVE_FAILURES > ALERT_THRESHOLD:
        print(f"!! 연속 실패 {CONSECUTIVE_FAILURES}회 발생! 디스코드 알림을 전송합니다.")
        message = (
            f"# 🚨 **서버 장애 알림 경고** 🚨\n"
            f"서버가 죽은 거 같아요!! 10분 내에 3번 이상 오면 API 노예들에게 알려주세요!!\n"
            f"> **URL**: `{HEALTH_CHECK_URL}`\n"
            f"> **연속 실패 횟수**: **{CONSECUTIVE_FAILURES}회**\n"
            f"> **마지막 확인 시간**: `{datetime.now(ZoneInfo('Asia/Seoul')).strftime(DATETIME_FORMAT)}`"
        )
        sendDiscordNotification(message)
        CONSECUTIVE_FAILURES = 3


if __name__ == "__main__":
    print("헬스 체크 스크립트 실행")
    print(f"대상 URL: {HEALTH_CHECK_URL}")

    while True:
        checkHealth()
        time.sleep(SLEEP_TIME)
