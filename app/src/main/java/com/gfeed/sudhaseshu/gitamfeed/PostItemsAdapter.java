package com.gfeed.sudhaseshu.gitamfeed;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostItemsAdapter  extends  RecyclerView.Adapter<PostItemsAdapter.PostItemsViewHolder>{

    private Context context;
    private List<PostItems> items;
    private FirebaseFirestore likeDatabase;
    private FirebaseFirestore db;

     PostItemsAdapter(Context context, List<PostItems> items) {
        this.context = context;
        this.items = items;
    }


    @NonNull
    @Override
    public PostItemsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.i("app","View created");
        View view = LayoutInflater.from(context).inflate(R.layout.cardview,viewGroup,false);

        return new PostItemsViewHolder(
                LayoutInflater.from(context).inflate(R.layout.cardview, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final PostItemsViewHolder postItemsViewHolder, int i) {
        final PostItems post = items.get(i);

        final String id = post.getId();

        if(haveNetworkConnection()) {

            final FirebaseAuth mAuth = FirebaseAuth.getInstance();
            likeDatabase = FirebaseFirestore.getInstance();
            db = FirebaseFirestore.getInstance();

            postItemsViewHolder.p_likecount.setText(post.getLikes());
            postItemsViewHolder.p_month.setText(post.getPost_month());
            postItemsViewHolder.p_date.setText(post.getPost_date());
            postItemsViewHolder.p_post.setText(post.getPost_content());
            postItemsViewHolder.title.setText(post.getTitle());

            try {

                //Get Likes Count
                likeDatabase.collection("Posts/" + id + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            int count = documentSnapshots.size();

                            postItemsViewHolder.updateLikesCount(count);
                            Log.i("app", String.valueOf(count));

                        } else {
                            postItemsViewHolder.updateLikesCount(0);
                        }

                    }
                });

                // Check bookmarks icon whether it is filled or not while Displaying
                likeDatabase.collection("Users/" + mAuth.getUid() + "/PostId's").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            if (!task.getResult().exists()) {
                                postItemsViewHolder.bookmark.setImageResource(R.drawable.bookmarknofill); // Changing the bookmark icon when the post is bookmarked
                            } else {
                                postItemsViewHolder.bookmark.setImageResource(R.drawable.bookmarkfill); // Changing the bookmark icon when the post is removed from bookmarks.
                            }
                        }
                        catch(Exception e){
                            Toast.makeText(context,"No Network",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Check like icon whether it is filled or not while displaying in Discussions
                likeDatabase.collection("Posts/" + id + "/Likes").document(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()) {

                            postItemsViewHolder.like_button.setImageResource(R.drawable.like1nofill); // Changing the like icon when the post is liked

                        } else {

                            postItemsViewHolder.like_button.setImageResource(R.drawable.like1fill);

                        }

                    }
                });


                //When bookmark button is clicked
                postItemsViewHolder.bookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("app", "" + mAuth.getUid());
                        likeDatabase.collection("Users/" + mAuth.getUid() + "/PostId's").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (!task.getResult().exists()) {
                                    postItemsViewHolder.bookmark.setImageResource(R.drawable.bookmarkfill); // Changing the bookmark icon when the post is bookmarked

                                    Map<String, Object> mark = new HashMap<>();
                                    mark.put("timestamp", FieldValue.serverTimestamp());

                                    likeDatabase.collection("Users/" + mAuth.getUid() + "/PostId's").document(id).set(mark);
                                } else {
                                    postItemsViewHolder.bookmark.setImageResource(R.drawable.bookmarknofill); // Changing the bookmark icon when the post is removed from bookmarks.
                                    likeDatabase.collection("Users/" + mAuth.getUid() + "/PostId's").document(id).delete();
                                }
                            }
                        });
                    }
                });

                //When like button is clicked
                postItemsViewHolder.like_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        likeDatabase.collection("Posts/" + id + "/Likes").document(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if (!task.getResult().exists()) {

                                    postItemsViewHolder.like_button.setImageResource(R.drawable.like1fill); // Changing the like icon when the post is liked

                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());

                                    db.collection("Posts").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            PostItems p = documentSnapshot.toObject(PostItems.class);
                                            p.setLikes(String.valueOf(Integer.valueOf(p.getLikes()) + 1));

                                            db.collection("Posts").document(id).set(p).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("app", "Liked");
                                                }
                                            });
                                        }
                                    });

                                    likeDatabase.collection("Posts/" + id + "/Likes").document(mAuth.getUid()).set(likesMap);

                                } else {

                                    postItemsViewHolder.like_button.setImageResource(R.drawable.like1nofill);

                                    db.collection("Posts").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            PostItems p = documentSnapshot.toObject(PostItems.class);
                                            p.setLikes(String.valueOf(Integer.valueOf(p.getLikes()) - 1));

                                            db.collection("Posts").document(id).set(p).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("app", "Liked");
                                                }
                                            });
                                        }
                                    });
                                    likeDatabase.collection("Posts/" + id + "/Likes").document(mAuth.getUid()).delete();

                                }

                            }
                        });

                    }

                });

                postItemsViewHolder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {

                            Intent intent = new Intent(context, Read_Post.class);
                            //Toast.makeText(context, "name:" + context, Toast.LENGTH_SHORT).show();
                            intent.putExtra("title", post.getTitle());
                            intent.putExtra("content", post.getPost_content());
                            intent.putExtra("day", post.getPost_date());
                            intent.putExtra("month", post.getPost_month());
                            intent.putExtra("likes", post.getLikes());
                            intent.putExtra("pid", post.getPid());

                            Log.i("test2", post.getPost_content());
                            context.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                postItemsViewHolder.p_date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {

                            Intent intent = new Intent(context, Read_Post.class);
                            intent.putExtra("title", post.getTitle());
                            intent.putExtra("content", post.getPost_content());
                            intent.putExtra("day", post.getPost_date());
                            intent.putExtra("month", post.getPost_month());
                            intent.putExtra("likes", post.getLikes());
                            intent.putExtra("pid", post.getPid());
                            Log.i("test2", post.getPost_content());
                            context.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                postItemsViewHolder.p_month.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {

                            Intent intent = new Intent(context, Read_Post.class);
                            intent.putExtra("title", post.getTitle());
                            intent.putExtra("content", post.getPost_content());
                            intent.putExtra("day", post.getPost_date());
                            intent.putExtra("month", post.getPost_month());
                            intent.putExtra("likes", post.getLikes());
                            intent.putExtra("pid", post.getPid());

                            Log.i("test2", post.getPost_content());
                            context.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                postItemsViewHolder.p_post.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {

                            Intent intent = new Intent(context, Read_Post.class);
                            intent.putExtra("title", post.getTitle());
                            intent.putExtra("content", post.getPost_content());
                            intent.putExtra("day", post.getPost_date());
                            intent.putExtra("month", post.getPost_month());
                            intent.putExtra("likes", post.getLikes());
                            intent.putExtra("pid", post.getPid());

                            Log.i("test2", post.getPost_content());
                            context.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else {
            Toast.makeText(context,"Network required",Toast.LENGTH_SHORT).show();
        }



    }


    @Override
    public int getItemCount() {
        return items.size();
    }

     class PostItemsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView p_date,p_month,p_post,title,p_likecount;
        ImageButton like_button,bookmark;

         PostItemsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            title = mView.findViewById(R.id.Title_problem);
            bookmark = mView.findViewById(R.id.bookmark);
            like_button = mView.findViewById(R.id.like);
            p_date = mView.findViewById(R.id.day);
            p_month = mView.findViewById(R.id.month);
            p_post = mView.findViewById(R.id.problem);
            p_likecount = mView.findViewById(R.id.like_count);

        }

         public void updateLikesCount(int count){
             p_likecount.setText(String.valueOf(count));
         }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
