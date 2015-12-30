package dk.botunge.hapifhirandroidsample;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.ServerValidationModeEnum;


public class PatientListActivity extends AppCompatActivity {

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);
        RecyclerView rw = (RecyclerView) findViewById(R.id.resource_list);
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rw.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)

        mAdapter = new MyAdapter();
        rw.setAdapter(mAdapter);
        load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patient_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void load() {
        new AsyncTask<Void, Void, ca.uhn.fhir.model.api.Bundle>(
        ) {
            @Override
            protected ca.uhn.fhir.model.api.Bundle doInBackground(Void... params) {
                try {
                    FhirContext fc = FhirContext.forDstu2();
                    //Skip retrieval of conformance statement...
                    fc.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
                    IGenericClient gc = fc.newRestfulGenericClient("http://spark-dstu2.furore.com/fhir"); //$NON-NLS-1$
                    return gc.search().forResource(Patient.class).execute();
                } catch (Throwable e) {
                    Log.e("dddd", "ddd", e);
                }
                return null;


            }

            @Override
            protected void onPostExecute(ca.uhn.fhir.model.api.Bundle o) {
                super.onPostExecute(o);
                List<Patient> list = o != null ? o.getResources(Patient.class) : Collections.<Patient>emptyList();
                mAdapter.setData(list);
            }
        }.execute();

    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private List<Patient> data = new ArrayList<>();

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter() {
        }

        public void setData(List<Patient> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patient_layout, parent, false);
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Patient patient = data.get(position);
            StringBuilder b = new StringBuilder("Identifier");
            for (IdentifierDt i : patient.getIdentifier()) {
                if (i.isEmpty()) {
                    continue;
                }
                b.append(" ").append(i.getValue());
            }
            holder.header.setText(b.toString().trim());
            holder.content.setText(Html.fromHtml(patient.getText().getDiv().getValueAsString()));

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView header;
            public TextView content;

            public ViewHolder(View v) {
                super(v);
                header = (TextView) v.findViewById(R.id.patient_header);
                content = (TextView) v.findViewById(R.id.patient_content);
            }
        }
    }
}
