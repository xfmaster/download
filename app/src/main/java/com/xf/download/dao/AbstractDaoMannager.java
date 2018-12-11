package com.xf.download.dao;

import java.util.List;

public abstract class AbstractDaoMannager<T> {
    abstract void insert(Class<T> tClass, T t);

    abstract void insert(Class<T> tClass, List<T> list);

    abstract void update(Class<T> tClass, T t);

    abstract void update(Class<T> tClass, List<T> list);

    abstract T query(Class<T> tClass, T key);

    abstract List<T> queryList(Class<T> tClass);

    abstract List<T> queryByKeyList(Class<T> tClass, String key, String value);

    abstract void delete(Class<T> tClass, T t);

    abstract void deleteAll(Class<T> tClass);
}
