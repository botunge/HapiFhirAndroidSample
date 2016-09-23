/**
 * Copyright 2015 Thomas Andersen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import ca.uhn.fhir.rest.server.EncodingEnum;


public class PatientListActivity extends AppCompatActivity {

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);
        RecyclerView rw = (RecyclerView) findViewById(R.id.resource_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rw.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter();
        rw.setAdapter(mAdapter);
        load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_patient_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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
                    //This should be put somewhere in the app, where the context is saved, instead of
                    //Retrieving it each time.
                    FhirContext fc = FhirContext.forDstu2();
                    //Skip retrieval of conformance statement...
                    fc.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
                    IGenericClient gc = fc.newRestfulGenericClient("http://spark-dstu2.furore.com/fhir"); //$NON-NLS-1$
                    // Comment this line to use XML instead of JSON
                    gc.setEncoding(EncodingEnum.JSON);
                    return gc.search().forResource(Patient.class).execute();
                } catch (Throwable e) {
                    Log.e("Err", "Err, handle this better", e);
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

        public void setData(List<Patient> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.patient_layout, parent, false);
            return new ViewHolder(v);
        }

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
            String html = patient.getText().getDiv().getValueAsString();
            if (html != null && html.length() > 0) {
                holder.content.setText(Html.fromHtml(html));
            }
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
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
