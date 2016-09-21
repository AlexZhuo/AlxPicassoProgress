package vc.zz.qduxsh.picassoprogress;

import java.util.List;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/2/28.
 */
public abstract class AlxListViewCommonAdapter<T> extends BaseAdapter {


    /**
     * 用其他的类继承这个类使用
     * @author Administrator
     * @param <T>
     *
     * @param <T>
     */

    protected Context mContext;//要让子类可以访问，所以要用protected
    protected List<T> mDatas;
    protected LayoutInflater mInflater;
    protected int xmlId;

    public AlxListViewCommonAdapter(Context context,int xmlId,List datas){
        this.mContext = context;
        this.mDatas = datas;
        this.mInflater = LayoutInflater.from(context);
        this.xmlId = xmlId;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mDatas.size();
    }

    public List<T> getmDatas() {
        return mDatas;
    }

    public void setmDatas(List<T> mDatas) {
        this.mDatas = mDatas;
    }

    @Override
    public T getItem(int position) {
        // TODO Auto-generated method stub
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public  View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder = ViewHolder.get(mContext,convertView,parent,xmlId,position);
        convert(holder,position,getItem(position));
        return holder.getConverView();
    };

    /**
     * 根据listView成员的数量动态设置listView的高度
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight =0;  //用来记录内部行的总高度
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        //获得参数对象
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        //内部元素的高度加上分割线的高度
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    /**
     *
     * @param holder 是getView中要用来返回的哪个holder，第二个参数是getItem(position)，是list<T>中的那个T，即每行的数据对象
     * @param t
     */
    public abstract void convert(ViewHolder holder,int position,T entity);

    public static class ViewHolder {
        private int mPosition;
        private View mConvertView;
        private SparseArray<View> mViews;

        private ViewHolder(Context context,ViewGroup parent,int layoutId, int position){
            this.mPosition = position;
            this.mViews = new SparseArray<View>();
            mConvertView = LayoutInflater.from(context).inflate(layoutId,parent,false);
            mConvertView.setTag(this);
        }

        public static ViewHolder get(Context context,View convertView,ViewGroup parent,int layoutId,int position){
            if(convertView==null){
                return new ViewHolder(context, parent, layoutId, position);
            }else {
                ViewHolder holder = (ViewHolder) convertView.getTag();
                holder.mPosition = position;
                return holder;
            }
        }

        //根据id，从xml中找出来
        public <E extends View> E getView(int viewId){
            View view = mViews.get(viewId);
            if(view==null){
                view = mConvertView.findViewById(viewId);
                mViews.put(viewId, view);

            }
            return (E)view;
        }
        public View getConverView(){
            return mConvertView;
        }

        /**
         * 根据textView的id和传来的字符串直接给这个textView赋值
         * @param viewId
         * @param text
         * @return
         */
        public ViewHolder setText(int viewId,String text){
            TextView tv = getView(viewId);
            tv.setText(text);
            return this;
        }


        public ViewHolder setImageResource(int viewId,int resId){
            ImageView view = getView(viewId);
            view.setImageResource(resId);
            return this;
        }

        public ViewHolder setImageBitmap(int viewId,Bitmap bitMap){
            ImageView view = getView(viewId);
            view.setImageBitmap(bitMap);
            return this;
        }

        /**
         * 只要这些方法写的够全，用起来就会很方便
         * @param viewId
         * @param url
         * @return
         */
//        public ViewHolder setImageURL(int viewId,String url){
//            ImageView view = getView(viewId);
//            //此处使用加载类进行网络加载和缓存
//            ImageLoader.getInstance().displayImage(url,view);
//            return null;
//        }


    }

}



