package com.example.studeezy.userDashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.studeezy.R;

import java.util.List;
import java.util.Map;

public class SubjectExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> yearList;
    private Map<String, List<String>> semesterMap;

    public SubjectExpandableListAdapter(Context context, List<String> yearList, Map<String, List<String>> semesterMap) {
        this.context = context;
        this.yearList = yearList;
        this.semesterMap = semesterMap;
    }

    @Override
    public int getGroupCount() {
        return yearList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String year = yearList.get(groupPosition);
        return semesterMap.get(year).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return yearList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String year = yearList.get(groupPosition);
        return semesterMap.get(year).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String year = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_list_group, null);
        }

        TextView yearTextView = convertView.findViewById(R.id.yearTextView);
        yearTextView.setText(year);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String child = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_list_child, null);
        }

        TextView childTextView = convertView.findViewById(R.id.childTextView);
        childTextView.setText(child);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        String child = (String) getChild(groupPosition, childPosition);

        if (child.contains("Semester")) {
            return false;
        } else {
            return true;
        }
    }
}