package edu.fsu.cs.mobile.top10;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class FeedAdapter extends ArrayAdapter {

    private static final String TAG = "FeedAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<FeedEntry> applications;



    /*what is a context?
        an interface to a global information about an application environment and allows access to
        specific application resources and classes.

        "holds a state of the application or activity while it's running

        context contains information on the things about the application or activity the
        system needs in order to manage it; as well as access to the various classes it might need
        to accomplish this.
     */

    /*what is a layoutinflater
       instantiates a layout xml file into it's corresponding view objects.

       creates all view objects which are described in the xml
     */


    public FeedAdapter(Context context, int resource, List<FeedEntry> applications) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.applications = applications;
    }


    @Override
    public int getCount() {
        return applications.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        /*created ViewHolder variable to hold the object*/
        ViewHolder viewHolder;


        /*
        * -initially we were creating an new inflateview each time the getView method was called
        * -however, we do not need to do this, because the listview provides us with a view
        * when it can.
        * -this is what convertview is for
        * - when there is a view to reuse the listview will pass a reference to convertview and use it
        * - until a view is scrolled off the screen, convertview will be null, i.e., no view to use
        * - so we need to check if convertview is null, and only then do we create a new one
        *
        * */
        if(convertView == null){
            convertView = layoutInflater.inflate(layoutResource, parent, false);

            /*viewHolder object created and then setting convertView tag to viewHolder*/
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else{
            /*-if convertView is not null
            * -we retrieve viewholder from it's tag using the getTag() method
            * -the tag is holding the ViewHolder object which is why we cast
            * because we know what it is */
            viewHolder = (ViewHolder) convertView.getTag();
        }
        /*
        * 1) create a view by inflating the layout resource via the inflater we created in the
        *    constructor
        * 2) find the three textview widgets via "view.findviewbyid"
        *    !!! want to find the id associated with this view
        *    !!! in the view declaration, layoutResource is the layout holding these views
        * 3) view oparameters:
        *   layoutResource =
        *   parent = the xml File we are importing to grab all the views
        *   false =
        * */
        // View view = layoutInflater.inflate(layoutResource, parent, false);

        /*
        * -findViewById is very costly
        * -when you reuse a view this can throttle the program
        * -rather than look for the textview widget everytime the layout is recycled
        *  we can just reference the widgets; this is what view holder pattern does
        * -it is called the viewholder pattern because it uses a class to hold the
        *  views we found the last cycle
        * -we need to create a class which holds the references to the widgets as well as
        *  store the instance of this class and viewobjects have a tag field which allow this
        * -going to create a new inner class called "ViewHolder" inside the FeedAdapter Class
        * -
        *
        * */
        //TextView tvName = convertView.findViewById(R.id.tvName);
        //TextView tvArtist = convertView.findViewById(R.id.tvArtist);
        //TextView tvSummary = convertView.findViewById(R.id.tvSummary);


        FeedEntry currentApp = applications.get(position);


        //setting values of the widgets as they are now held in the ViewHolder class
        viewHolder.tvName.setText(currentApp.getName());
        viewHolder.tvArtist.setText(currentApp.getArtist());
        //viewHolder.tvSummary.setText(currentApp.getSummary());

        return convertView;



    }

    private class ViewHolder{
        final TextView tvName;
        final TextView tvArtist;
        final TextView tvSummary;

        ViewHolder(View v){
            this.tvName = v.findViewById(R.id.tvName);
            this.tvArtist = v.findViewById(R.id.tvArtist);
            this.tvSummary = v.findViewById(R.id.tvSummary);
        }

    }

}
