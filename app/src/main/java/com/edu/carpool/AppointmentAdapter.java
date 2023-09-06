package com.edu.carpool;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.MyViewHolder>{
    private final AppointmentRecyclerViewInterface appointmentRecyclerViewInterface;

    Context context;
    ArrayList<AppointmentModelClass> appointmentModelClass;

    public AppointmentAdapter(Context context, ArrayList<AppointmentModelClass> appointmentModelClass, AppointmentRecyclerViewInterface appointmentRecyclerViewInterface){
        this.context = context;
        this.appointmentModelClass = appointmentModelClass;
        this.appointmentRecyclerViewInterface = appointmentRecyclerViewInterface;
    }

    @Override
    public AppointmentAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // This is where you inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.history_cardview, parent, false);
        return new AppointmentAdapter.MyViewHolder(view, appointmentRecyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(AppointmentAdapter.MyViewHolder holder, int position) {
        // assigning values to the views we created in the recycler view layout file
        // based on the position of the recycler view
        holder.tv_history_driver_name.setText(appointmentModelClass.get(position).getDriver_name());
        holder.tv_history_from_input.setText(appointmentModelClass.get(position).getFrom_address());
        holder.tv_history_to_input.setText(appointmentModelClass.get(position).getTo_address());
        holder.tv_history_to_schedule_input.setText(appointmentModelClass.get(position).getDate_time());
        holder.tv_history_custom_input.setText(appointmentModelClass.get(position).getCustom_request());
        holder.tv_history_status_input.setText(appointmentModelClass.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        // How many items we have in total
        return appointmentModelClass.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_history_driver_name, tv_history_from_input, tv_history_to_input, tv_history_to_schedule_input,
                tv_history_custom_input, tv_history_status_input;
        public MyViewHolder(View itemView,AppointmentRecyclerViewInterface appointmentRecyclerViewInterface) {
            super(itemView);

            tv_history_driver_name = itemView.findViewById(R.id.tv_history_driver_name);
            tv_history_from_input = itemView.findViewById(R.id.tv_history_from_input);
            tv_history_to_input = itemView.findViewById(R.id.tv_history_to_input);
            tv_history_to_schedule_input = itemView.findViewById(R.id.tv_history_to_schedule_input);
            tv_history_custom_input = itemView.findViewById(R.id.tv_history_custom_input);
            tv_history_status_input = itemView.findViewById(R.id.tv_history_status_input);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (appointmentRecyclerViewInterface != null){
                        int pos = getAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION){
                            appointmentRecyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
