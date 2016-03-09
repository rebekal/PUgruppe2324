import RPi.GPIO as GPIO
import time
GPIO.setmode(GPIO.BCM)


TRIG = 23
ECHO = 24

outputPin = 4

def measureDistance():
	GPIO.setup(TRIG,GPIO.OUT)
	GPIO.setup(ECHO,GPIO.IN)

	GPIO.output(TRIG, False)
	time.sleep(2)

	GPIO.output(TRIG, True)
	time.sleep(0.00001)
	GPIO.output(TRIG, False)

	while GPIO.input(ECHO)==0:
  		pulse_start = time.time()

	while GPIO.input(ECHO)==1:
  		pulse_end = time.time()

	pulse_duration = pulse_end - pulse_start

	distance = pulse_duration * 17150

	distance = round(distance, 2) // i cm
	GPIO.cleanup()
	return distance

def light_led():
    GPIO.setup(outputPin, GPIO.OUT)

    while True:
        if measureDistance() > 0:
            GPIO.output(outputPin, GPIO.HIGH)
        else:
            GPIO.output(outputPin, GPIO.LOW)

def main():
    while True:
        print measureDistance()
        light_led()



main()