package com.avermedia.training.contentsharing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final static String TAG = "MainActivity";
	private final static String FILE_NAME = "test";
	private final static String FILE_SUFFIX = ".jpg";
	private final static String ACTION_RETURN_FILE = "com.avermedia.training.ACTION_RETURN_FILE";
	
	private final static String PROVIDER_AUTH = "com.avermedia.training.contentsharing.fileprovider";
	
	private Intent mDefaultIntent;
	private ShareActionProvider mShareActionProvider;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Get intent, action and MIME type
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();
	    
	    mDefaultIntent = new Intent();
	    mDefaultIntent.setAction(Intent.ACTION_SEND);
	    mDefaultIntent.putExtra(Intent.EXTRA_TEXT, "Content: " + TAG);
	    mDefaultIntent.putExtra(Intent.EXTRA_SUBJECT, TAG);
	    mDefaultIntent.setType("text/plain");

        // Set the Activity's result to null to begin with
        setResult(Activity.RESULT_CANCELED, null);

	    if (Intent.ACTION_SEND.equals(action) && type != null) {
	        if ("text/plain".equals(type)) {
	            handleSendText(intent); // Handle text being sent
	        } else if (type.startsWith("image/")) {
	            handleSendImage(intent); // Handle single image being sent
	        }
	    } 
	    else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
	        if (type.startsWith("image/")) {
	            handleSendMultipleImages(intent); // Handle multiple images being sent
	        }
	    } 
	    else if (Intent.ACTION_PICK.equals(action)) {
	    	((TextView)findViewById(R.id.title)).setText(R.string.txt_provider);
	    }
	    else if (ACTION_RETURN_FILE.equals(action)) {
	    	
	    }
	    else {
	        // Handle other intents, such as being started from the home screen
	    }
		
		
		((Button)findViewById(R.id.btn_simple_sharing)).
				setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String txt = ((TextView)findViewById(R.id.txt_simple_sharing)).getText().toString();
				
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "Content: " + txt);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, txt);
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
			}
		});
		
		((Button)findViewById(R.id.btn_simple_sharing1)).
				setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String txt = ((TextView)findViewById(R.id.txt_simple_sharing1)).getText().toString();
				
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "Content: " + txt);
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, txt);
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.custom_title)));
			}
		});
		
		((Button)findViewById(R.id.btn_simple_sharing2)).
				setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String txt = ((TextView)findViewById(R.id.txt_simple_sharing2)).getText().toString();
								
				ArrayList<Uri> imageUris = new ArrayList<Uri>();
				// Add your image URIs here
				imageUris.add(
						Uri.parse("http://developer.android.com/images/training/sharing/share-text-screenshot.png")); 
				imageUris.add(
						Uri.parse("http://developer.android.com/images/training/sharing/share-text-screenshot.png"));

				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
				shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
				shareIntent.putExtra(Intent.EXTRA_TEXT, "Content: " + txt);
				shareIntent.setType("image/*");
				startActivity(Intent.createChooser(shareIntent, "Share images to.."));
			}
		});
		
		((Button)findViewById(R.id.btn_simple_sharing3)).
				setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri fileUri = null;
				String txt = ((TextView)findViewById(R.id.txt_simple_sharing3)).getText().toString();
				
			    // Set up an Intent to send back to apps that request a file
			    Intent resultIntent = new Intent(ACTION_RETURN_FILE);
		        // Get the files/ subdirectory of internal storage
		        File privateRootDir = getFilesDir();
		        // Get the files/images subdirectory;
		        File imagesDir = new File(privateRootDir, "images");
		        if(!imagesDir.exists()) {
		        	imagesDir.mkdir();
		        }
		        
		        getTempFile(MainActivity.this, imagesDir, FILE_NAME);
		        
		        // Get the files in the images subdirectory
		        File []imageFiles = imagesDir.listFiles();
				
				if(null == imageFiles || imageFiles.length <= 0)
					return;
				/*
                 * Get a File for the selected file name.
                 */
                File requestFile = imageFiles[0];
                
                /*
                 * Most file-related method calls need to be in
                 * try-catch blocks.
                 */
                try {
                    // Use the FileProvider to get a content URI
                    fileUri = FileProvider.getUriForFile(
                            MainActivity.this,
                            PROVIDER_AUTH,
                            requestFile);
                } catch (IllegalArgumentException e) {
                    Log.e("File Selector",
                          "The selected file can't be shared: " +
                        		  txt);
                }
                
                if (fileUri != null) {
                    // Grant temporary read permission to the content URI
                	resultIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                	
                    // Put the Uri and MIME type in the result Intent
                	resultIntent.setDataAndType(
                            fileUri,
                            getContentResolver().getType(fileUri));
                	
                    // Set the result
                    MainActivity.this.setResult(Activity.RESULT_OK,
                    		resultIntent);
                } else {
                	resultIntent.setDataAndType(null, "");
                    MainActivity.this.setResult(RESULT_CANCELED,
                    		resultIntent);
                }
                
                finish();
			}
		});
		
		((Button)findViewById(R.id.btn_simple_sharing4)).
				setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				
				
		        Intent requestFileIntent;
		        requestFileIntent = new Intent(Intent.ACTION_PICK);
		        requestFileIntent.setType("image/jpg");
				startActivityForResult(requestFileIntent, 0);
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// Locate MenuItem with ShareActionProvider
	    MenuItem item = menu.findItem(R.id.menu_item_share);
	    
		// Fetch and store ShareActionProvider
	    mShareActionProvider = (ShareActionProvider) item.getActionProvider();
	    setShareIntent(mDefaultIntent);
		return true;
	}
	
	/**
     * When the Activity of the app that hosts files sets a result and calls
     * finish(), this method is invoked. The returned Intent contains the
     * content URI of a selected file. The result code indicates if the
     * selection worked or not.
     * 
     * @param requestCode
     * @param resultCode
     * @param returnIntent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
            Intent returnIntent) {
        // If the selection didn't work
        if (resultCode != RESULT_OK) {
            // Exit without doing anything else
            return;
        } else {
            // Get the file's content URI from the incoming Intent
            Uri returnUri = returnIntent.getData();
            String mimeType = getContentResolver().getType(returnUri);
            Cursor returnCursor =
                    getContentResolver().query(returnUri, null, null, null, null);
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            int nameIndex = returnCursor.
            			getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.
            			getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            Toast.makeText(MainActivity.this, "File name: " 
            		+ returnCursor.getString(nameIndex)
            		+ " type: " + mimeType
            		+ " size: " + returnCursor.getLong(sizeIndex), 
            			Toast.LENGTH_SHORT).show();
            
            ParcelFileDescriptor input = null;
            /*
             * Try to open the file for "read" access using the
             * returned URI. If the file isn't found, write to the
             * error log and return.
             */
            try {
                /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
            	input = getContentResolver().openFileDescriptor(returnUri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("MainActivity", "File not found.");
                return;
            }
            
            if(null != input) {
            	/*
            	 *  Read or write the file by using file descriptor, i.e., FileReader.
            	 */
//            	FileDescriptor fd = input.getFileDescriptor();
            }
            
        }
    }
    
    /**
     * Create an empty temporary file. 
     * @param context
     * @param dir
     * @param url
     * @return file
     */
    private File getTempFile(Context context, File dir, String url) {
        File file = null;
        try {
            String fileName = Uri.parse(url).getLastPathSegment();
            file = File.createTempFile(fileName, FILE_SUFFIX, dir);
        }
        catch (IOException e) {
            // Error while creating file
        	e.printStackTrace();
        }
        return file;
    }
	
	/**
	 * Call to update the share intent
	 * @param shareIntent
	 */
	private void setShareIntent(Intent shareIntent) {
	    if (mShareActionProvider != null) {
	        mShareActionProvider.setShareIntent(shareIntent);
	    }
	}
	
	/**
	 * Call to update the share intent
	 * @param shareIntent
	 */
	void handleSendText(Intent intent) {
	    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	    if (sharedText != null) {
	        // Update UI to reflect text being shared
	    	Log.d(TAG, "handleSendText : " + sharedText.toString() );
	    }
	}

	/**
	 * Call to update the share intent
	 * @param shareIntent
	 */
	void handleSendImage(Intent intent) {
	    Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (imageUri != null) {
	        // Update UI to reflect image being shared
	    	Log.d(TAG, "handleSendImage : " + imageUri.toString() );
	    }
	}

	/**
	 * Call to update the share intent
	 * @param shareIntent
	 */
	void handleSendMultipleImages(Intent intent) {
	    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	    if (imageUris != null) {
	        // Update UI to reflect multiple images being shared
	    	for(Uri uri : imageUris) {
	    		Log.d(TAG, "handleSendMultipleImages : " + uri.toString() );
	    	}
	    }
	}

}
