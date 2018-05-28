package com.example.hector.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MensajesAdapter extends ArrayAdapter<Mensaje> {
    public MensajesAdapter(Context context, ArrayList<Mensaje> mensajes) {
        super(context, 0, mensajes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Mensaje mensaje = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mensaje, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.nombre);
        TextView tvMensaje = (TextView) convertView.findViewById(R.id.mensaje);
        TextView tvDate = (TextView) convertView.findViewById(R.id.date);
        // Populate the data into the template view using the data object
        tvName.setText(mensaje.usuario);
        tvMensaje.setText(mensaje.msg);
        tvDate.setText(mensaje.date);
        // Return the completed view to render on screen
        return convertView;
    }
}