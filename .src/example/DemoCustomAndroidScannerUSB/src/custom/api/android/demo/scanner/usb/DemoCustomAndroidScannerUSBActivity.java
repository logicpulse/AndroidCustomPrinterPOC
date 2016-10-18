//************************************************************************************
//*                                                                                  *
//* This document contains programming examples.                                     *
//*                                                                                  *
//* CUSTOM S.p.A. grants you a nonexclusive copyright license to use                 *
//* all programming code examples from which you can generate similar                *
//* function tailored to your own specific needs.                                    *
//*                                                                                  *
//* All sample code is provided by CUSTOM S.p.A. for illustrative purposes           *
//* only. These examples have not been thoroughly tested under all conditions.       *
//* CUSTOM S.p.A., therefore, cannot guarantee or imply reliability,                 *
//* serviceability, or function of these programs.                                   *
//*                                                                                  *
//* In no event shall CUSTOM S.p.A. be liable for any direct, indirect,              *
//* incidental, special, exemplary, or consequential damages (including, but not     *
//* limited to, procurement of substitute goods or services; loss of use, data,      *
//* or profits; or business interruption) however caused and on any theory of        *
//* liability, whether in contract, strict liability, or tort (including negligence  *
//* or otherwise) arising in any way out of the use of this software, even if        *
//* advised of the possibility of such damage.                                       *
//*                                                                                  *
//* All programs contained herein are provided to you "as is" without any            *
//* warranties of any kind.                                                          *
//* The implied warranties of non-infringement, merchantability and fitness for a    *
//* particular purpose are expressly disclaimed.                                     *
//*                                                                                  *
//************************************************************************************

package custom.api.android.demo.scanner.usb;

import it.custom.printer.api.android.*;
import android.hardware.usb.*;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Locale;

import custom.api.android.demo.usb.R;

public class DemoCustomAndroidScannerUSBActivity extends Activity 
{		
	private int GETSTATUS_TIME = 500;		//0.5 sec
	
	public ScannerImage selectedImagePath = null;
	
	static UsbDevice[] usbDeviceList = null;
	static CustomPrinter prnDevice = null;
	
	static ListView listDevicesView ;  
	static ArrayAdapter<String> listAdapter;  
	
	static int lastDeviceSelected = -1;
	static int deviceSelected = -1;
	
	static int iCurrentFeederStatus = CustomPrinter.SCANNER_FEEDER_DISABLE;
	
	private String lock="lockAccess";
	
    static Handler hGetStatus = new Handler();
    
    static int iImageID = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);               
        
        //Get API Version
        String strAPIVersion = CustomAndroidAPI.getAPIVersion();
                        
        //Change Application Title
        setTitle(getResources().getString(R.string.app_name) + " - API Rel "+strAPIVersion);
        
        //Start the get status thread after GETSTATUS_TIME msec
        hGetStatus.postDelayed(GetStatusRunnable, GETSTATUS_TIME);
        
        //Init everything
        InitEverything(savedInstanceState);
    }    

    
    
    private void InitEverything(Bundle savedInstanceState)
    {
    	//If is the 1st time
    	if (savedInstanceState == null)
    	{
    		try
            {
            	//Get the list of devices
            	usbDeviceList = CustomAndroidAPI.EnumUsbDevices(this);

            	if ((usbDeviceList == null) || (usbDeviceList.length == 0))
            	{
            		//Show Error
            		showAlertMsg("Error...", "No Devices Connected...");            		
            		return;
            	}                               	
            }
    		catch(CustomException e )
            {
            	
            	//Show Error
            	showAlertMsg("Error...", e.getMessage());
        		return;
            }
            catch(Exception e )
            {
            	
            	//Show Error
            	showAlertMsg("Error...", "Enum devices error...");
        		return;
            }            
    	}
    	    	
    	// Find the ListView resource.   
    	listDevicesView = (ListView)findViewById( R.id.listViewDevices );  
      
        // Create and populate a List of Devices
        String[] strDevices = new String[usbDeviceList.length];
        for (int i=0;i<usbDeviceList.length;i++)
        {
        	strDevices[i] = (i+1)+". USB Device VID: 0x" +  IntToHexString(usbDeviceList[i].getVendorId(),4) + " PID: 0x"+IntToHexString(usbDeviceList[i].getProductId(),4);	            	
        }
        
        ArrayList<String> devicesList = new ArrayList<String>();  
        devicesList.addAll( Arrays.asList(strDevices) );  
          
        // Create ArrayAdapter using the list.  
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, devicesList);  	              	              
          
        // Set the ArrayAdapter as the ListView's adapter.  
        listDevicesView.setAdapter( listAdapter );          
        
        listDevicesView.setItemsCanFocus(false);        
        listDevicesView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);    
        deviceSelected = 0;
        listDevicesView.setItemChecked(deviceSelected, true); //Select the 1st
        listDevicesView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
        	{				
        		//Save position Value
        		deviceSelected = arg2;				
			}
		});
    }    
    
    public void onPrintPictureClick(View view)  
	{  
    	//open device
    	if (OpenDevice() == false)
    		return;
    	
    	//Check image
    	if (selectedImagePath == null)
    	{
    		showAlertMsg("Error...", "Select a Picture to Print...");
    		return;
    	}        
    	
    	synchronized (lock) 
		{
    		//***************************************************************************
    		// PRINT PICTURE
    		//***************************************************************************
    		
	    	try
	        {
	    		//Print (Left Align and Fit to printer width)
	    		prnDevice.printImage(selectedImagePath.Image,CustomPrinter.IMAGE_ALIGN_TO_LEFT, CustomPrinter.IMAGE_SCALE_TO_FIT, 0);
	        }
	    	catch(CustomException e )
            {            	
            	//Show Error
            	showAlertMsg("Error...", e.getMessage());        		
            }
			catch(Exception e )
	        {
				showAlertMsg("Error...", "Print Picture Error...");
	        }
			
			//***************************************************************************
			// FEEDS and CUT
			//***************************************************************************
			
			try
	        {
				//Feeds (3)
				prnDevice.feed(3);
	    		//Cut (Total)
	    		prnDevice.cut(CustomPrinter.CUT_TOTAL);	    		
	        }
	    	catch(CustomException e )
            {    
	    		//Only if isn't unsupported
	    		if (e.GetErrorCode() != CustomException.ERR_UNSUPPORTEDFUNCTION)
	    		{
	    			//Show Error
	    			showAlertMsg("Error...", e.getMessage());
	    		}
            }
			catch(Exception e )
	        {
				showAlertMsg("Error...", "Print Picture Error...");
	        }
			
			//***************************************************************************
			// PRESENT
			//***************************************************************************
			
			try
	        {				
	    		//Present (40mm)
	    		prnDevice.present(40);
	        }
	    	catch(CustomException e )
            {    
	    		//Only if isn't unsupported
	    		if (e.GetErrorCode() != CustomException.ERR_UNSUPPORTEDFUNCTION)
	    		{
	    			//Show Error
	    			showAlertMsg("Error...", e.getMessage());
	    		}
            }
			catch(Exception e )
	        {
				showAlertMsg("Error...", "Print Picture Error...");
	        }
		}
	} 
    
    public void onbuttonFeeder(View view)  
	{   
    	//open device
    	if (OpenDevice() == false)
    		return;
    	
    	Button bFeeder = (Button)findViewById(R.id.buttonFeeder);
    	if (iCurrentFeederStatus == CustomPrinter.SCANNER_FEEDER_DISABLE)
    	{
    		iCurrentFeederStatus = CustomPrinter.SCANNER_FEEDER_ENABLE;    		
    		bFeeder.setText( getResources().getString(R.string.btnfeeder_disable));
    	}
    	else
    	{
    		iCurrentFeederStatus = CustomPrinter.SCANNER_FEEDER_DISABLE;
    		bFeeder.setText( getResources().getString(R.string.btnfeeder_enable));
    	}
    	
    	synchronized (lock) 
		{
	    	try
	        {
	    		prnDevice.scannerFeeder(iCurrentFeederStatus);
	        }
	    	catch(CustomException e )
            {            	
            	//Show Error
            	showAlertMsg("Error...", e.getMessage());
            }
			catch(Exception e )
	        {
				showAlertMsg("Error...", "Feeder Command Error...");
	        }
		}
	} 
    
    public void GetScannerImage()  
	{   
    	//open device
    	if (OpenDevice() == false)
    		return;    	    	
    	
    	selectedImagePath = null;
    	int inumretry = 0;
		String strError = "";
    	

		do
		{
    		try
	        {
    			//***************************************************************************
        		// GET BW IMAGE
        		//*************************************************************************** 		
    			selectedImagePath = prnDevice.scannerGetImage(CustomPrinter.SCANNER_IMAGE_BW, false);
    			break;
	        }
    		catch(CustomException e )
            {            	
    			inumretry++;
    			strError = e.getMessage();
    			try{Thread.sleep(200);}catch(Exception ee){}
            	
            }
		}
		while(inumretry < 5);	
    	
		if (inumretry >= 5)
		{
			//Show Error
        	showAlertMsg("Error...", strError);
        	return;
		}
		
		
		
    	try
        {
    		ScannerStatus scsts = prnDevice.scannerGetFullStatus();
    		
    		//Fill infos:	    		
    		ImageView imgscan = (ImageView) findViewById(R.id.imgscan); 
    		imgscan.setImageBitmap(selectedImagePath.Image);
    		
    		String strTextInfo = "";	    		
    		strTextInfo+="Image Width:" + selectedImagePath.ImageWidth + "\n";
    		strTextInfo+="Image Height:" + selectedImagePath.ImageHeight + "\n";
    		strTextInfo+="Image Bits per Pixel:" + selectedImagePath.ImageBitsPerPixel + "\n";
    		strTextInfo+="Image N.Colors:" + selectedImagePath.ImageNumColors + "\n";
    		strTextInfo+="Image Inclination:" + selectedImagePath.ImageInclination + "\n";
    		
    		if (scsts.stsSCANNERCARDFOUND)
    		{
    			String strCard = "";
    			try
    			{
    				//***************************************************************************
    	    		// GET CARD INFORMATIONS
    	    		//***************************************************************************
    				ScannerCardData scData = prnDevice.scannerGetCardData();
    				strCard = "Card ID:"+scData.iCardID+" Data:"+BufferToString(scData.bOriginalBuffer);
    			}
    			catch(Exception ee)
    			{}
    			strTextInfo+="Card Found:"+strCard+"\n";
    		}
    		if (scsts.stsSCANNERBARCODEFOUND)
    		{
    			String strBarcode = "";
    			try
    			{
    				//***************************************************************************
    	    		// GET BARCODE DATA
    	    		//***************************************************************************
    				byte[] bbufferBarcode = prnDevice.scannerGetBarcode();
    				strBarcode = BufferToString(bbufferBarcode);
    			}
    			catch(Exception ee)
    			{}
    			strTextInfo+="Barcode Found:"+strBarcode+"\n";
    		}
    		
    		TextView t=(TextView)findViewById(R.id.imgscantext);
    		t.setText(strTextInfo);
        }	    	
		catch(Exception e )
        {
			showAlertMsg("Error...", "Get Image Error...");
        }

	} 
    
    
    
    public void onExit(View view) throws Throwable  
	{
    	try
    	{
    		if (prnDevice != null)
    		{
    			//Close device
    			prnDevice.close();
    		}
    	}
    	catch(CustomException e )
        {            	
        	//Show Error
        	showAlertMsg("Error...", e.getMessage());
        }
		catch(Exception e )
        {			
        }
    	
    	//Force Close
    	android.os.Process.killProcess(android.os.Process.myPid());
	}
    
    private Runnable GetStatusRunnable = new Runnable() 
    {
        public void run() 
        {
        	String printerName;
        	int deviceShowStatus = View.INVISIBLE;
        	CheckBox ckbox;
        	TextView txtView;
        	
        	
        	//If the device is open
        	if (prnDevice != null)
        	{
        		synchronized (lock) 
    			{
        			try
	        		{
        				//Get Image ID. When it's changed, a new image is available to be read
        				int i = prnDevice.scannerGetImageScannedID();
        				if (i != iImageID)
        				{
        					//Read the Image
        					GetScannerImage();
        					iImageID = i;
        				}
	        		}
	        		catch(CustomException e )
	                {
	                	
	                }
	        		catch(Exception e)
	        		{
	        			
	        		}
    			}
        		
        		synchronized (lock) 
    			{
	        		try
	        		{
	        			
	        			//Get printer Status
	        			PrinterStatus prnSts = prnDevice.getPrinterFullStatus();
	        			
	        			//Check it: NOPAPER
	        			ckbox = (CheckBox)findViewById( R.id.checkBoxNOPAPER);        			        		
	        			ckbox.setChecked(prnSts.stsNOPAPER);
	        			
	        			//Check it: PAPER ROLLING
	        			ckbox = (CheckBox)findViewById( R.id.checkBoxROLLING);        			        		
	        			ckbox.setChecked(prnSts.stsPAPERROLLING);
	        			
	        			//Check it: LF KEY PRESSED
	        			ckbox = (CheckBox)findViewById( R.id.checkBoxLF);        			        		
	        			ckbox.setChecked(prnSts.stsLFPRESSED);
	        			
	        			//Get printer name
	        			printerName = prnDevice.getPrinterName();
	        			
	        			//Show Text PrinterName
	        			txtView = (TextView)findViewById( R.id.textPrinterName);        			        		
	        			txtView.setText("Printer Name:" + printerName + " (" +prnDevice.getPrinterInfo()+")");
	        			
	        			deviceShowStatus = View.VISIBLE;
	        			
	        		}
	        		catch(CustomException e )
	                {
	                	
	                }
	        		catch(Exception e)
	        		{
	        			
	        		}
    			}
        	}
        		
        	//Show / Hide Check NOPAPER
			ckbox = (CheckBox)findViewById( R.id.checkBoxNOPAPER);        			        		
			ckbox.setVisibility(deviceShowStatus);
			
			//Show / Hide Check PAPER ROLLING
			ckbox = (CheckBox)findViewById( R.id.checkBoxROLLING);        			        		
			ckbox.setVisibility(deviceShowStatus);
			
			//Show / Hide Check LF KEY PRESSED
			ckbox = (CheckBox)findViewById( R.id.checkBoxLF);        			        		
			ckbox.setVisibility(deviceShowStatus);
			
			//Show / Hide Text PrinterName
			txtView = (TextView)findViewById( R.id.textPrinterName);        			        		
			txtView.setVisibility(deviceShowStatus);
         
        	//run again in GETSTATUS_TIME msec
        	hGetStatus.postDelayed(GetStatusRunnable, GETSTATUS_TIME);
        }
    };
    
    //Open the device if it isn't already opened
    public boolean OpenDevice()
    {
    	//Device not selected
    	if (deviceSelected == -1)
    	{
    		showAlertMsg("Error...", "No Printer Device Selected...");
    		return false;
    	}
    	
    	//If i changed the device
    	if (lastDeviceSelected != -1)
    	{
    		if (deviceSelected != lastDeviceSelected)
    		{
    			try
                {
    				//Force close
    				prnDevice.close();
                }
    			catch(CustomException e )
                {
                	
                	//Show Error
                	showAlertMsg("Error...", e.getMessage());
            		return false;
                }
    			catch(Exception e )
                {
        			//Show error
        			return false;
                }
    			prnDevice = null;
    		}
    	}
    	
    	//If i never open it
    	if (prnDevice == null)
    	{
    		try
            {    			    			
    			//Open and connect it
    			prnDevice = new CustomAndroidAPI().getPrinterDriverUSB(usbDeviceList[deviceSelected], this);
    			
    			//Check this device supports scanner functions
    			if (!prnDevice.isScannerAvailable())
    			{
    				//Show Error
                	showAlertMsg("Error...", "No scanner available");
            		return false;
    			}
    			
    			//Set autoretract
    			prnDevice.scannerSetEndScanMovement(CustomPrinter.SCANNER_AUTOM_RETRACT);
    			//Search barcode only
    			prnDevice.scannerSetSearchType(CustomPrinter.SCANNER_SEARCH_TYPE_BARCODE, true);
    			
    			//Save last device selected
    			lastDeviceSelected = deviceSelected;    			    			
    			return true;
            }
    		catch(CustomException e )
            {
            	
            	//Show Error
            	showAlertMsg("Error...", e.getMessage());
        		return false;
            }
    		catch(Exception e )
            {
    			showAlertMsg("Error...", "Open Print Error...");
    			//open error
    			return false;
            }
    		    		
    	}
    	//Already opened
    	return true;
    	
    }
    
    private String BufferToString(byte[] bBuffer)
    {
    	String strBufferOut = "";
    	
    	for(int i=0;i<bBuffer.length;i++)
    	{
    		int iValue;
    		Byte bvalue = bBuffer[i];
    		if (bvalue >= 0) 
    			iValue = (int)bvalue;
    		else 
    			iValue = (int)(bvalue + 256);
    		
    		if ((iValue < 0x20) || (iValue > 0x7F))
    		{
    			//Write Hex
    			strBufferOut += "["+IntToHexString(iValue,2)+"]";
    		}
    		else
    		{
    			
    			strBufferOut += (char)(iValue);
    		}
    	}
    	
    	return strBufferOut;
    }
    
    private String IntToHexString(int iValue, int num0chars) 
    {
    	
        String hexString;
        
        //convert to Hex
        hexString = Integer.toHexString(iValue);
        int num0towrite = num0chars - hexString.length();
        //If i need to add some "0" before
        if (num0towrite > 0)
        {
        	for (int i=0;i<num0towrite;i++)
        		hexString = "0" + hexString;
        }
        
        //Change to upper case
        hexString = hexString.toUpperCase();
        
        return hexString;    
    }      
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) 
    {
    	super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.		        		
    }
    	
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) 
    {
    	super.onRestoreInstanceState(savedInstanceState);
    	//Restore UI state from the savedInstanceState.
    	// This bundle has also been passed to onCreate.
    	    	
    }
    
    void showAlertMsg(String title,String msg)
	{
    	AlertDialog.Builder dialogBuilder;    	
		dialogBuilder = new AlertDialog.Builder(this);    	
    	
		dialogBuilder.setNeutralButton( "OK", new DialogInterface.OnClickListener() 
		{			
			public void onClick(DialogInterface dialog, int which) {				
				dialog.dismiss();				
			}
		});
		
		dialogBuilder.setTitle(title);    	
		dialogBuilder.setMessage(msg);    	
		dialogBuilder.show();
    	
	}    
}