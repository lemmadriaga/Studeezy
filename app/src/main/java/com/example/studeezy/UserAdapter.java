package com.example.studeezy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private ArrayList<User> userList;
    private Context context;

    public UserAdapter(ArrayList<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);


        String formattedExpirationDate = formatDate(user.getExpirationDate());


        holder.name.setText("Name: " + user.getName());
        holder.email.setText("Email: " + user.getEmail());
        holder.premiumStatus.setText(user.isHasPremium() ? "Premium" : "Regular");
        holder.expirationDate.setText("Expires: " +
                (user.getExpirationDate() != null && user.getExpirationDate() > 0
                        ? formattedExpirationDate
                        : "No Subscription"));


        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserDetailsActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("hasPremium", user.isHasPremium());
            intent.putExtra("expirationDate", user.getExpirationDate());
            intent.putExtra("createdAt", user.getCreatedAt());
            context.startActivity(intent);
        });


        holder.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {

                                    FirebaseDatabase.getInstance()
                                            .getReference("users")
                                            .child(user.getId())
                                            .removeValue()
                                            .addOnSuccessListener(aVoid2 -> {

                                                userList.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, userList.size());
                                                Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete user from Realtime Database: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }


    private String formatDate(Long timestamp) {
        if (timestamp != null && timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            Date date = new Date(timestamp);
            return sdf.format(date);
        }
        return "N/A";
    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, premiumStatus, expirationDate;
        Button btnViewDetails, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            email = itemView.findViewById(R.id.textEmail);
            premiumStatus = itemView.findViewById(R.id.textPremiumStatus);
            expirationDate = itemView.findViewById(R.id.textExpirationDate);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
    public void updateList(ArrayList<User> filteredList) {
        this.userList = filteredList;
        notifyDataSetChanged();
    }

}
