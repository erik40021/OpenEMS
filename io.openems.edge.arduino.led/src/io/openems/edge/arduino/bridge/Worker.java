package io.openems.edge.arduino.bridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;


import io.openems.common.exceptions.OpenemsException;
import io.openems.common.worker.AbstractCycleWorker;
import io.openems.edge.arduino.led.RedArduinoLed;
import io.openems.edge.arduino.led.api.ArduinoLed;
import io.openems.edge.common.channel.BooleanWriteChannel;
import io.openems.edge.common.channel.IntegerReadChannel;

public class Worker extends AbstractCycleWorker {

	private final Logger logger = LoggerFactory.getLogger(Worker.class);
	private final RedArduinoLed parent;
	private final SerialPort port;


	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	
	public Worker(RedArduinoLed redArduinoLed, SerialPort commPort) {
		this.parent = redArduinoLed;
		this.port = commPort;
		try {
			this.openStreams();
		} catch (OpenemsException e) {
			e.printStackTrace();
		}
	}
	
	public void deactivate() {
		//close ports
		if(inputStream != null && outputStream != null) {
			this.closeAll();
		}
		super.deactivate();
	}
	
	public void onExecuteWrite() {
		 Boolean turn_led_on = this.readWriteChannel();
		 try {
			this.applyLedChange(turn_led_on);
		} catch (OpenemsException e) {
			this.parent.logError(this.logger, e.getMessage());
		}
	}

	private Boolean readWriteChannel() {
		BooleanWriteChannel setLedChannel = this.parent.channel(ArduinoLed.ChannelId.SET_LED);
		return setLedChannel.getNextValue().get();
	}

	private void applyLedChange(Boolean turn_led_on) throws OpenemsException {
		try {
			this.logger.info("Trying to write on port "+this.port.getSystemPortName());
			if(turn_led_on != null && turn_led_on) {
				this.logger.info("Writing 1 on port "+this.port.getSystemPortName());
				outputStream.write(1);
			}
			else {
				this.logger.info("Writing 0 on port "+this.port.getSystemPortName());
				outputStream.write(0);
			}
		} catch (IOException e) {
			this.parent.logError(this.logger, e.getMessage()+"IOException caught while trying to write on port");
		}
	}

	@Override
	protected void forever() throws Throwable {
		System.err.println("before reading serial port [ArduinoWorker]");
		IntegerReadChannel voltageChannel = this.parent.channel(ArduinoLed.ChannelId.VOLTAGE);
		// calculate voltage
		byte[] currently_written_bytes = new byte[1000];	//store up to the first 1000 bytes from serial port
		// reason for that is that there can be written a lot more bytes onto the serial port than just one while one cycle runs through
		int max_fill = inputStream.read(currently_written_bytes);
		int active_voltage = currently_written_bytes[max_fill-1];
		this.logger.info("Active voltage received: "+active_voltage);
		// Set the Channel-Value
		voltageChannel.setNextValue(active_voltage); 
	}
	
	private void closeInput() {	
		try {
			this.inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void closeOutput() {	
		try {
			this.outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeAll() {
		this.closeInput();
		this.closeOutput();
		this.port.closePort();	
	}

	/**
	 * Opens and Gets the Data Scanner.
	 * 
	 * @return the Scanner instance
	 * @throws OpenemsException on error
	 */
	private void openStreams() throws OpenemsException {
		this.openAndGetPort();
		this.inputStream = this.port.getInputStream();
		this.outputStream = this.port.getOutputStream();
	}
	/**
	 * Opens and Gets the SerialPort.
	 * 
	 * @return the SerialPort instance
	 * @throws OpenemsException on error
	 */
	private void openAndGetPort() throws OpenemsException {
		SerialPort port = this.port;
		port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
		port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
		port.setBaudRate(9600);
		if (port.isOpen()) {
			// Port was already open -> return
			return;
		}
		
		if (this.port.openPort()) {
			// Successfully opened Port
			System.err.println("successfully opened port "+port.getSystemPortName());
			return;
		}

		throw new OpenemsException("Unable to open the port [" + port.getSystemPortName() + "]");
	}
	
	
	

}