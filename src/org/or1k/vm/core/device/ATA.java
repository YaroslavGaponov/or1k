package org.or1k.vm.core.device;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.or1k.vm.core.Device;
import org.or1k.vm.core.IDevice16;
import org.or1k.vm.core.IDevice8;
import org.or1k.vm.core.VM;
import org.or1k.vm.events.EventType;

public class ATA extends Device implements IDevice8, IDevice16 {

	private FileChannel channel;
	private ShortBuffer image;
	private ByteBuffer image8;

	private final int heads = 16;
	private final int sectors = 64;
	private int cylinders, nsectors;

	// ATA command block registers
	// 2 is the reg_shift
	private final int ATA_REG_DATA = 0x00 << 2; // data register
	private final int ATA_REG_ERR = 0x01 << 2; // error register, feature
	// register
	private final int ATA_REG_NSECT = 0x02 << 2; // sector count register
	private final int ATA_REG_LBAL = 0x03 << 2; // sector number register
	private final int ATA_REG_LBAM = 0x04 << 2; // cylinder low register
	private final int ATA_REG_LBAH = 0x05 << 2; // cylinder high register
	private final int ATA_REG_DEVICE = 0x06 << 2; // drive/head register
	private final int ATA_REG_STATUS = 0x07 << 2; // status register // command
	// register

	private final int ATA_REG_FEATURE = ATA_REG_ERR; // and their aliases
	// (writing)
	private final int ATA_REG_CMD = ATA_REG_STATUS;
	private final int ATA_REG_BYTEL = ATA_REG_LBAM;
	private final int ATA_REG_BYTEH = ATA_REG_LBAH;
	private final int ATA_REG_DEVSEL = ATA_REG_DEVICE;
	private final int ATA_REG_IRQ = ATA_REG_NSECT;

	// device control register
	private final int ATA_DCR_RST = 0x04; // Software reset (RST=1, reset)
	private final int ATA_DCR_IEN = 0x02; // Interrupt Enable (IEN=0, enabled)

	// ----- ATA (Alternate) Status Register
	private final int ATA_SR_BSY = 0x80; // Busy
	private final int ATA_SR_DRDY = 0x40; // Device Ready
	private final int ATA_SR_DF = 0x20; // Device Fault
	private final int ATA_SR_DSC = 0x10; // Device Seek Complete
	private final int ATA_SR_DRQ = 0x08; // Data Request
	private final int ATA_SR_COR = 0x04; // Corrected data (obsolete)
	private final int ATA_SR_IDX = 0x02; // (obsolete)
	private final int ATA_SR_ERR = 0x01; // Error

	private int DCR;
	private int DR;
	private int SCR;
	private int SNR;
	private int SR;
	private int FR;
	private int ER;
	private int CR;

	private int lcyl;
	private int hcyl;
	private int select;
	private boolean driveselected;

	private ShortBuffer IDENTIFY_DEVICE = ShortBuffer.allocate(256);

	private ShortBuffer buffer;
	private int start, stop;

	public ATA(String fileName) {	
		Path path2 = Paths.get(fileName);		
		try {
			byte[] data = Files.readAllBytes(path2);
			image =ByteBuffer.wrap(data).asShortBuffer();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		Path path = Paths.get(fileName);
		try {

			channel = FileChannel.open(path, StandardOpenOption.READ,
					StandardOpenOption.WRITE);
			//image8 = channel.map(MapMode.READ_WRITE, 0, Files.size(path));
			//image = image8.asShortBuffer();

			cylinders = 60;///*image.capacity()*/1024*1024 / (heads * sectors * 512);
			nsectors = heads * sectors * cylinders;

			reset();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write16(int addr, int value) {
		//System.out.println(String.format("write16 addr %s value %s", addr, value));
		
		if (addr == 0) { // data register
			buffer.put(start++, (short) (value & 0xffff));
			
			if (start == stop) {
				SR = ATA_SR_DRDY | ATA_SR_DSC;
				//if ((DCR & ATA_DCR_IEN) == 0) {
					VM.EE.fire(EventType.RAISE_INTERRUPT, 15);
				//}
				if ((CR == 0x30) && (SNR > 1)) {
					SNR--;
					SetSector(GetSector() + 1);
					stop += 256;
					SR = ATA_SR_DRDY | ATA_SR_DSC | ATA_SR_DRQ;
				}

			}
		}
	}

	@Override
	public int read16(int addr) {
		
		//System.out.println(String.format("read16 addr %s", addr));
		
		if (addr == 0) { // data register
			//int val = buffer.get(start++);

			int val = buffer.get(start++) & 0xffff;
			
		    if ((start-1)>=1024 & (start-1)<=1044) {
		        System.out.println((start-1) + " = "  + val);
		    }
			//System.out.println(start + " : " + val + " " + buffer.get(start));
			
			if (start == stop) {
				SR = ATA_SR_DRDY | ATA_SR_DSC; // maybe no DSC for identify command but it works

				if ((CR == 0x20) && (SNR > 1)) {
					SNR--;
					SetSector(GetSector() + 1);
					stop += 256;
					SR = ATA_SR_DRDY | ATA_SR_DSC | ATA_SR_DRQ;
					//if ((DCR & ATA_DCR_IEN) == 0) {
						VM.EE.fire(EventType.RAISE_INTERRUPT, 15);
					//}
				}

			}
			return val;
		}
		return 0;
	}

	private int Swap16(int i) {
		return (((i & 0xFF) << 8) | ((i >>> 8) & 0xFF));
	}

	@Override
	public void write8(int addr, int value) {

		//System.out.println(String.format("write8 addr %s value %x", addr, value));
		
		if (addr == ATA_REG_DEVICE) {
			//System.out.println("ATADev: Write drive/head register");
			DR = value;
	        //System.out.println(String.format("Head %s",(value&0xF)));
	        //System.out.println(String.format("Drive No. %s",(value>>4)&1));
	        //System.out.println(String.format("LBA Mode %s",(value>>6)&1));
			
			driveselected = ((value>>>4)&1) == 0;
			return;
		}

		if (addr == 0x100) { //device control register
			//System.out.println("ATADev: Write CTL register");
			if (((value & ATA_DCR_RST) == 0) && ((DCR & ATA_DCR_RST) != 0)) { // reset done
				//System.out.println("ATADev: drive reset done");
				DR &= 0xF0; // reset head
				SR = ATA_SR_DRDY | ATA_SR_DSC;
				SCR = 0x1;
				SNR = 0x1;
				lcyl = 0x0;
				hcyl = 0x0;
				ER = 0x1;
				CR = 0x0;
			} else
				if (((value & ATA_DCR_RST) != 0) && ((DCR & ATA_DCR_RST) == 0)) { // reset
					//System.out.println("ATADev: drive reset");
					ER = 0x1; // set diagnostics message
					SR = ATA_SR_BSY | ATA_SR_DSC;
				}

			DCR = value;
			return;
		}

		if (!driveselected) {
			return;
		}

		switch(addr)
		{
		case ATA_REG_FEATURE:
			//System.out.println("ATADev: Write feature register");
			FR = value;
			break;

		case ATA_REG_NSECT:
			//System.out.println("ATADev: Write sector count register: " + value);
			SNR = value;
			break;

		case ATA_REG_LBAL:
			//System.out.println("ATADev: Write sector number register:" + value);
			SCR = value;
			break;

		case ATA_REG_LBAM:
			//System.out.println("ATADev: Write cylinder low register " + value);
			lcyl = value;
			break;

		case ATA_REG_LBAH:
			//System.out.println("ATADev: Write cylinder high number register");
			hcyl = value;
			break;
		case ATA_REG_CMD:
			//System.out.println("ATADev: Write Command register");
			CR = value;
			ExecuteCommand();
			break;
		}
		
	}

	@Override
	public int read8(int addr) {
		//System.out.println(String.format("read8 addr %s", addr));
		
		if (!driveselected) {
			return 0xFF;
		}
		switch (addr) {
		case ATA_REG_ERR:
			//System.out.println("ATADev: read error register");
			return ER;
		case ATA_REG_NSECT:
			//System.out.println("ATADev: read sector count register");
			return SNR;
		case ATA_REG_LBAL:
			//System.out.println("ATADev: read sector number register");
			return SCR;
		case ATA_REG_LBAM:	
			//System.out.println("ATADev: read cylinder low register");
			return lcyl;
		case ATA_REG_LBAH:
			//System.out.println("ATADev: read cylinder high register");
			return hcyl;
		case ATA_REG_DEVICE:
			//System.out.println("ATADev: read drive/head register");
			return DR;

		case ATA_REG_STATUS:
			//System.out.println("ATADev: read status register");
			VM.EE.fire(EventType.CLEAR_INTERRUPT, 15);
			return SR;

		case 0x100: // device control register, but read as status register
			//System.out.println("ATADev: read alternate status register");
			return SR;
		}
		//System.out.println("errrror"); System.exit(0);
		return 0;
	}

	@Override
	public void reset() {

		DCR = 0x8; // fourth bis is always set
		DR = 0xA0; // some bits are always set to one
		SCR = 0x1;
		SNR = 0x1;
		SR = ATA_SR_DRDY; // status register
		FR = 0x0; // Feature register
		ER = 0x1; // Error register
		CR = 0x0; // Command register

		lcyl = 0x0;
		hcyl = 0x0;
		select = 0xA0;
		driveselected = true; // drive no 0

		IDENTIFY_DEVICE.clear();
		IDENTIFY_DEVICE.position(0);
		IDENTIFY_DEVICE.put((short) 0x0040);
		IDENTIFY_DEVICE.put((short) cylinders);
		IDENTIFY_DEVICE.put((short) 0);
		IDENTIFY_DEVICE.put((short) heads);
		IDENTIFY_DEVICE.put((short) (512 * sectors));
		IDENTIFY_DEVICE.put((short) 512);
		IDENTIFY_DEVICE.put((short) sectors);

		IDENTIFY_DEVICE.position(20);
		IDENTIFY_DEVICE.put((short) 0x0003);
		IDENTIFY_DEVICE.put((short) 512);
		IDENTIFY_DEVICE.put((short) 4);

		IDENTIFY_DEVICE.position(27);
		IDENTIFY_DEVICE.put((short) 0x6A6F);
		IDENTIFY_DEVICE.put((short) 0x7231);
		IDENTIFY_DEVICE.put((short) 0x6B2D);
		IDENTIFY_DEVICE.put((short) 0x6469);
		IDENTIFY_DEVICE.put((short) 0x736B);
		for (int i = 32; i <= 46; i++) {
			IDENTIFY_DEVICE.put((short) 0x2020);
		}

		IDENTIFY_DEVICE.position(47);
		IDENTIFY_DEVICE.put((short) (0x8000 | 128));
		IDENTIFY_DEVICE.put((short) 0x0000);
		IDENTIFY_DEVICE.put((short) (1 << 9));
		IDENTIFY_DEVICE.put((short) 0x0000); 
		IDENTIFY_DEVICE.put((short) 0x0200);
		IDENTIFY_DEVICE.put((short) 0x0200);
		IDENTIFY_DEVICE.put((short) 0);
		IDENTIFY_DEVICE.put((short) cylinders);
		IDENTIFY_DEVICE.put((short) heads);
		IDENTIFY_DEVICE.put((short) sectors);
		IDENTIFY_DEVICE.put((short) (nsectors & 0xFFFF));
		IDENTIFY_DEVICE.put((short) ((nsectors >>> 16) & 0xFFFF));
		IDENTIFY_DEVICE.put((short) 0x0000);
		IDENTIFY_DEVICE.put((short) (nsectors & 0xFFFF));
		IDENTIFY_DEVICE.put((short) ((nsectors >>> 16) & 0xFFFF));

		IDENTIFY_DEVICE.position(80);
		IDENTIFY_DEVICE.put((short)((1 << 1) | (1 << 2)));
		IDENTIFY_DEVICE.put((short)0x0000);
		IDENTIFY_DEVICE.put((short)(1 << 14));
		IDENTIFY_DEVICE.put((short)(1 << 14));
		IDENTIFY_DEVICE.put((short)(1 << 14));
		IDENTIFY_DEVICE.put((short)(1 << 14));
		IDENTIFY_DEVICE.put((short)0x0000);
		IDENTIFY_DEVICE.put((short) (1 << 14));

		buffer = IDENTIFY_DEVICE;
		start = 0;
		stop = 256;
		
		for(int i=0; i<IDENTIFY_DEVICE.capacity();i++)
			IDENTIFY_DEVICE.put(i, (short)Swap16(IDENTIFY_DEVICE.get(i)));
		
	}

	private void ExecuteCommand()
	{
		switch(CR)
		{
		case 0xEC: // identify device
			//System.out.println("identify device");
			buffer = IDENTIFY_DEVICE;
			start = 0; 
			stop = 256; 
			SR = ATA_SR_DRDY | ATA_SR_DSC | ATA_SR_DRQ;
			if ((DCR & ATA_DCR_IEN) == 0) {
				VM.EE.fire(EventType.RAISE_INTERRUPT, 15);
			}
			break;

		case 0x91: // initialize drive parameters
			//System.out.println("initialize drive parameters");
			SR = ATA_SR_DRDY | ATA_SR_DSC;
			ER = 0x0;
			if ((DCR & ATA_DCR_IEN) == 0) {
				VM.EE.fire(EventType.RAISE_INTERRUPT, 15);
			}
			break;

		case 0x20: // load sector
		case 0x30: // save sector
			//System.out.println("load/save sector");
			if (SNR == 0) {
				SNR = 256;
			}
			buffer = image;
			start = GetSector() * 256;
			stop = start + 256;
			
			// error in  12818432
			System.out.println("load/save sector " + start + " - " + stop + " " + SNR + " lcyl=" + lcyl + " hcyl=" + hcyl + " DR="  + (DR&0x0F) + " SCR=" + SCR);

			SR = ATA_SR_DRDY | ATA_SR_DSC | ATA_SR_DRQ;
			ER = 0x0;
			if (CR == 0x20) {
				if ((DCR & ATA_DCR_IEN) == 0) {
					VM.EE.fire(EventType.RAISE_INTERRUPT, 15);
				}
			}
			break;

		case 0xC4: // read multiple sectors
		case 0xC5: // write multiple sectors
			//System.out.println("load/save sectors");
			if (SNR == 0) {
				SNR = 256;
			}

			buffer = image;
			start = GetSector() * 256;
			stop = start + 256 * SNR;
			
			System.out.println("load/save sectors " + start + " - " + stop + " " + SNR);

			SR = ATA_SR_DRDY | ATA_SR_DSC | ATA_SR_DRQ;
			ER = 0x0;
			if (CR == 0xC4) {
				if ((DCR & ATA_DCR_IEN) == 0) {
					VM.EE.fire(EventType.RAISE_INTERRUPT, 15);
				}
			}

			break;
		}
	}


	private int GetSector() {
		if ((DR & 0x40) != 0) {
			return ((DR&0x0F) << 24) | (hcyl << 16) | (lcyl << 8) | SCR;
		}
		System.out.println("getsector error"); System.exit(0);
		return -1;
	}

	private void SetSector(int sector) {
		if ((DR & 0x40)!= 0) {
			SCR = sector & 0xFF;
			lcyl = (sector >> 8) & 0xFF;
			hcyl = (sector >> 16) & 0xFF;
			DR = (DR & 0xF0) | ((sector >> 24) & 0x0F);
		} else {
			System.out.println("setsector error"); System.exit(0);
		}
	}



}
