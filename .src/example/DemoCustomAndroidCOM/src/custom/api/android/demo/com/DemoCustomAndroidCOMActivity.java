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

package custom.api.android.demo.com;

import it.custom.printer.api.android.*;

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

public class DemoCustomAndroidCOMActivity extends Activity 
{
	private int INT_SELECT_PICTURE = 1;
	
	private int GETSTATUS_TIME = 1000;		//1sec
	
	public String selectedImagePath = "";
	
	static String[] comPortsList = null;
	static CustomPrinter prnDevice = null;
	
	static ListView listDevicesView ;  
	static ArrayAdapter<String> listAdapter;  
	
	static int lastDeviceSelected = -1;
	static int deviceSelected = -1;
	
	private String lock="lockAccess";
	
    static Handler hGetStatus = new Handler();
	
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
    		//Fill the list with only 1 port
    		comPortsList = new String[1];
    		comPortsList[0] = "/dev/ttymxc0";
                    
    	}
    	    	
    	// Find the ListView resource.   
    	listDevicesView = (ListView)findViewById( R.id.listViewDevices );  
      
        // Create and populate a List of Devices
        String[] strDevices = new String[comPortsList.length];
        for (int i=0;i<comPortsList.length;i++)
        {
        	strDevices[i] = (i+1)+". COM PORT : " + comPortsList[i];	            	
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
    
    private String getPath(Uri uri) 
    {
    	String imagePath;
    	
    	//1st, i try to convert it if it was into the gallery
    	try
    	{    	    	    
    		//if the Uri 
    		String[] projection = { MediaStore.Images.Media.DATA };
    		Cursor cursor = managedQuery(uri, projection, null, null, null);
    		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    		cursor.moveToFirst();
    		imagePath = cursor.getString(column_index);
    	}
    	catch(Exception e)
    	{
    		//it fails so it is a standard file
    		imagePath = uri.getPath();
    	}
    	
    	String imagePathLCase = imagePath.toLowerCase(Locale.ENGLISH);
    	//check file extension
    	if ( (imagePathLCase.contains("jpg")) || (imagePathLCase.contains("jpeg")) || (imagePathLCase.contains("bmp")) || (imagePathLCase.contains("tiff")) || (imagePathLCase.contains("gif")) || (imagePathLCase.contains("png")))
    			return imagePath;    	
    	else
    		return "";
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) 
        {
        	//If i select the picture, save the path
            if (requestCode == INT_SELECT_PICTURE) 
            {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri); 
                Button printPictureButton = (Button) findViewById(R.id.buttonPrintPicture);
                //change button text
                if (selectedImagePath != "")
                	printPictureButton.setText(this.getString(R.string.printpicture) + " (" + selectedImagePath+")");
                else
                	printPictureButton.setText(this.getString(R.string.printpicture));
            }            
        }
    }
    
    public void onSelectPictureClick(View view)  
	{  
    	//Open the Gallery to select the picture
		Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");                
        startActivityForResult(Intent.createChooser(intent,null), INT_SELECT_PICTURE);      
	}
    
    public void onPrintPictureClick(View view)  
	{  
    	//open device
    	if (OpenDevice() == false)
    		return;
    	
    	//Check image
    	if (selectedImagePath == "")
    	{
    		showAlertMsg("Error...", "Select a Picture to Print...");
    		return;
    	}
    	
    	//Create the Bitmap    	    
    	Bitmap image = BitmapFactory.decodeFile(selectedImagePath);
    	
    	synchronized (lock) 
		{
    		//***************************************************************************
    		// PRINT PICTURE
    		//***************************************************************************
    		
	    	try
	        {
	    		//Print (Left Align and Fit to printer width)
	    		prnDevice.printImage(image,CustomPrinter.IMAGE_ALIGN_TO_LEFT, CustomPrinter.IMAGE_SCALE_TO_FIT, 0);
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
    
    public void onPrintText(View view)  
	{  
    	PrinterFont fntPrinterNormal = new PrinterFont();
    	PrinterFont fntPrinterBold2X = new PrinterFont();
    	String strTextToPrint;
    	//open device
    	if (OpenDevice() == false)
    		return;
    	
    	//Get Text
    	strTextToPrint = ((EditText)findViewById( R.id.EditTextToPrint)).getText().toString();
    	
    	try
        {
	    	//Fill class: NORMAL
	    	fntPrinterNormal.setCharHeight(PrinterFont.FONT_SIZE_X1);					//Height x1
	    	fntPrinterNormal.setCharWidth(PrinterFont.FONT_SIZE_X1);					//Width x1
	    	fntPrinterNormal.setEmphasized(false);										//No Bold
	    	fntPrinterNormal.setItalic(false);											//No Italic
	    	fntPrinterNormal.setUnderline(false);										//No Underline
	    	fntPrinterNormal.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER);	//Center
	    	fntPrinterNormal.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT);		//Default International Chars
	    	
	    	//Fill class: BOLD size 2X
	    	fntPrinterBold2X.setCharHeight(PrinterFont.FONT_SIZE_X2);					//Height x2
	    	fntPrinterBold2X.setCharWidth(PrinterFont.FONT_SIZE_X2);					//Width x2
	    	fntPrinterBold2X.setEmphasized(true);										//Bold
	    	fntPrinterBold2X.setItalic(false);											//No Italic
	    	fntPrinterBold2X.setUnderline(false);										//No Underline
	    	fntPrinterBold2X.setJustification(PrinterFont.FONT_JUSTIFICATION_CENTER);	//Center	    	
	    	fntPrinterBold2X.setInternationalCharSet(PrinterFont.FONT_CS_DEFAULT);		//Default International Chars	    	
        }
    	catch(CustomException e )
        {
        	
        	//Show Error
        	showAlertMsg("Error...", e.getMessage());
        }
    	catch(Exception e )
        {
    		showAlertMsg("Error...", "Set font properties error...");
        }
    	
    	//***************************************************************************
		// PRINT TEXT
		//***************************************************************************
    	
    	synchronized (lock) 
		{
	    	try
	        {
	    		//Print Text (NORMAL)
	    		prnDevice.printText(strTextToPrint, fntPrinterNormal);
	    		prnDevice.printTextLF(strTextToPrint, fntPrinterNormal);
	    		//Print Text (BOLD size 2X)
	    		prnDevice.printTextLF(strTextToPrint, fntPrinterBold2X);
	        }
	    	catch(CustomException e )
            {            	
            	//Show Error
            	showAlertMsg("Error...", e.getMessage());
            }
			catch(Exception e )
	        {
				showAlertMsg("Error...", "Print Text Error...");
	        }
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
    		//Init COM port running linux commands
    		try 
    		{
    		    Runtime.getRuntime().exec("chmod 666 " + comPortsList[deviceSelected]);    		    
    		} 
    		catch (Exception e) 
    		{
    		
    		}
    		
    		try
            {    			    			
    			//Open and connect it
    			prnDevice = new CustomAndroidAPI().getPrinterDriverCOM(comPortsList[deviceSelected]);    			
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