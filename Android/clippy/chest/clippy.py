def install_or_import():
	try:
		import pyperclip
	except ImportError:
		import pip
		pip.main(['install', 'pyperclip'])
	finally:
		global pyperclip
		import pyperclip

	try:
		import time
	except ImportError:
		import pip
		pip.main(['install', 'time'])
	finally:
		global time
		import time

	try:
		import datetime
	except ImportError:
		import pip
		pip.main(['install', 'datetime'])
	finally:
		global datetime
		import datetime


if __name__ == '__main__':
	install_or_import()

	recent_value = ""
	while True:
		tmp_value = pyperclip.paste()

		if tmp_value != recent_value:
			timestamp = '[{:%d-%m-%Y-%H:%M:%S}]'.format(datetime.datetime.now())
			recent_value = tmp_value
			print(timestamp + " " + recent_value)
		time.sleep(2)
