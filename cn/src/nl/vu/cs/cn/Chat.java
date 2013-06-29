package nl.vu.cs.cn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Chat extends Activity {
	private EditText txt_name;
	public static String name;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Connect various GUI components to the networking stack.
		Button btn_conn = new Button(this);
		btn_conn = (Button)findViewById(R.id.btn_connect);
		txt_name = (EditText)findViewById(R.id.txt_name);
		
		btn_conn.setOnClickListener(new OnClickListener() {
	         public void onClick(View v) {
	        	 name = txt_name.getText().toString();
	        	 Toast.makeText(Chat.this, "Welcome "+name, Toast.LENGTH_SHORT).show();
	        	 Intent myIntent = new Intent(Chat.this, MessagingActivity.class);
	        	 myIntent.putExtra("name", name);
	        	 Chat.this.finish();
	        	 startActivity(myIntent);
	         }
       });
		
	}

}
