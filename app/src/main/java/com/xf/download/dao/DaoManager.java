package com.xf.download.dao;

import android.content.Context;


import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;

import java.util.List;

public class DaoManager<T> extends AbstractDaoMannager<T> {
    private static DaoManager mInstance;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;

    public static void initeDao(Context mContext) {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(mContext, "sofa-db", null);
        mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
    }

    private DaoManager() {

    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public static DaoManager getInstance() {
        if (mInstance == null) {
            mInstance = new DaoManager();
        }
        return mInstance;
    }

    private AbstractDao<T, T> getDao(Class<T> tClass) {
        try {
            return (AbstractDao<T, T>) mDaoMaster.newSession().getDao(tClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void insert(Class<T> tClass, T t) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao != null) {
            dao.delete(t);
            dao.insertOrReplace(t);
        }
    }

    @Override
    public void insert(Class<T> tClass, List<T> list) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao != null) {
            dao.deleteAll();
            dao.insertOrReplaceInTx(list);
        }
    }

    @Override
    public void update(Class<T> tClass, T t) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao != null)
            dao.update(t);
    }

    @Override
    public void update(Class<T> tClass, List<T> list) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao != null)
            dao.updateInTx(list);
    }

    @Override
    public T query(Class<T> tClass, T key) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao != null)
            return dao.load(key);
        return null;
    }

    @Override
    public List<T> queryList(Class<T> tClass) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao != null)
            return dao.loadAll();
        return null;
    }

    @Override
    public List<T> queryByKeyList(Class<T> tClass, String key, String value) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao == null)
            return null;
        Property[] properties = dao.getProperties();
        if (properties == null || properties.length == 0) return null;
        for (Property property : properties) {
            if (property.name.equals(key)) {
                return dao.queryBuilder().where(property.eq(value)).build().list();
            }
        }
        return null;
    }

    @Override
    public void delete(Class<T> tClass, T t) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao == null)
            return;
        dao.delete(t);
    }

    @Override
    public void deleteAll(Class<T> tClass) {
        AbstractDao<T, T> dao = getDao(tClass);
        if (dao == null)
            return;
        dao.deleteAll();
    }
}
