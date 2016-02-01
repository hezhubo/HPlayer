package com.hezb.hplayer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hezb.hplayer.R;
import com.hezb.hplayer.base.BaseFragment;
import com.hezb.hplayer.constant.ConstantKey;
import com.hezb.hplayer.entity.FileInfo;
import com.hezb.hplayer.ui.activity.PlayerActivity;
import com.hezb.hplayer.ui.adapter.FileListAdapter;
import com.hezb.hplayer.util.FileManager;
import com.hezb.hplayer.util.FileOperate;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件列表页
 * Created by hezb on 2016/1/22.
 */
public class FileListFragment extends BaseFragment {

    private TextView mFolderPath;
    private View mBackPrevious;
    private ListView mFileList;

    private SharedPreferences mSharedPreferences;
    private SearchFileTask mSearchFileTask;

    private String currentPath;// 当前路径
    private int currentSelection;// 列表选中项目

    private List<Integer> selectionList;// 选中列表

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_file_list;
    }

    @Override
    protected void initAllMember() {
        mFolderPath = (TextView) findViewById(R.id.folder_path);
        mBackPrevious = findViewById(R.id.back_to_previous_folder);
        mFileList = (ListView) findViewById(R.id.file_list);

        // 初始化当前路径
        mSharedPreferences = mContext.getSharedPreferences(
                ConstantKey.H_PLAYER, Context.MODE_PRIVATE);
        currentPath = mSharedPreferences.getString(ConstantKey.FOLDER_PATH,
                Environment.getExternalStorageDirectory().getPath());

        selectionList = new ArrayList<>();

        mBackPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = currentPath.lastIndexOf("/");
                if (index != -1 && index != 0) {
                    currentPath = currentPath.substring(0, index);
                    if (selectionList.size() > 0) { // 是点击进入的，现在返回上级目录，要选中之前的点击项
                        currentSelection = selectionList.get(selectionList.size() - 1);
                        selectionList.remove(selectionList.size() - 1);
                    } else {
                        currentSelection = 0;
                    }
                    startSearch();
                }
            }
        });
        mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileInfo fileInfo = (FileInfo) parent.getItemAtPosition(position);
                if (fileInfo.getType() == FileInfo.TYPE_FOLDER) {
                    selectionList.add(position);
                    currentSelection = 0;
                    currentPath = fileInfo.getPath();
                    startSearch();

                } else if (fileInfo.getType() == FileInfo.TYPE_VIDEO) {
                    Intent intent = new Intent(mContext, PlayerActivity.class);
                    intent.putExtra(ConstantKey.PLAY_URL, fileInfo.getPath());
                    intent.putExtra(ConstantKey.NAME, fileInfo.getName());
                    startActivity(intent);
                }
            }
        });

        startSearch();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSearchFileTask != null) {
            mSearchFileTask.cancel(true);
        }
        mSharedPreferences.edit().putString(ConstantKey.FOLDER_PATH, currentPath).apply();
    }

    /**
     * 开始搜索文件
     */
    private void startSearch() {
        mSearchFileTask = new SearchFileTask();
        mSearchFileTask.execute(currentPath);
        mBackPrevious.setEnabled(false);
        mFileList.setEnabled(false);
    }

    /**
     * 刷新列表UI
     */
    private void setFileListUI(List<FileInfo> fileInfoList, int selection) {
        FileListAdapter adapter = new FileListAdapter(mContext, fileInfoList);
        mFileList.setAdapter(adapter);
        mFileList.setSelection(selection);
        mFolderPath.setText(currentPath);
        mBackPrevious.setEnabled(true);
        mFileList.setEnabled(true);
    }

    /**
     * 搜索文件任务
     */
    class SearchFileTask extends AsyncTask<String, Integer, List<FileInfo>> {

        @Override
        protected List<FileInfo> doInBackground(String... params) {
            List<FileInfo> fileInfoList = FileManager.getFilesList(params[0]);
            FileOperate fileOperate = new FileOperate(mContext, fileInfoList);
            fileOperate.sortListItem(FileOperate.TYPE_SORT_NAME);
            return fileInfoList;
        }

        @Override
        protected void onPostExecute(List<FileInfo> fileInfoList) {
            if (fileInfoList == null) {
                fileInfoList = new ArrayList<>();
            }
            setFileListUI(fileInfoList, currentSelection);
        }
    }

}
