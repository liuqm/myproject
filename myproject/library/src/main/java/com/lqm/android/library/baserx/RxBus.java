package com.lqm.android.library.baserx;
import android.support.annotation.NonNull;
import android.util.Log;


import com.lqm.android.library.commonutils.LogUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
//################################使用方法#####################################
/**
 * UserEvent event = new UserEvent(22, "lqm");
 * Observable<UserEvent> observable = RxBus.getInstance().register(this);
 * RxBus.getInstance().OnEvent(observable, new Action1<Object>() {
 *
 * @Override public void call(Object o) {
 * UserEvent event = (UserEvent) o;
 * LogUtils.logd("lqm", event.getName());
 * }
 * });
 * RxBus.getInstance().post(this,event);
 */

public class RxBus {
    private volatile static RxBus instance;

    public static RxBus getInstance() {
        if (null == instance) {
            synchronized (RxBus.class) {
                instance = new RxBus();
            }
        }
        return instance;
    }

    private RxBus() {
    }

    @SuppressWarnings("rawtypes")
    private ConcurrentHashMap<Object, List<Subject>> subjectMapper = new ConcurrentHashMap<Object, List<Subject>>();

    /**
     * 订阅事件源
     *
     * @param mObservable
     * @param mAction1
     * @return
     */
    public RxBus OnEvent(Observable<?> mObservable, Action1<Object> mAction1) {
        mObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(mAction1, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return getInstance();
    }

    /**
     * 注册事件源
     *
     * @param tag
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    public <T> Observable<T> register(@NonNull Object tag) {
        List<Subject> subjectList = subjectMapper.get(tag);
        if (null == subjectList) {
            subjectList = new ArrayList<Subject>();
            subjectMapper.put(tag, subjectList);
        }
        Subject<T, T> subject;
        subjectList.add(subject = PublishSubject.create());
        LogUtils.logd("register" + tag + "  size:" + subjectList.size());
        return subject;
    }

    @SuppressWarnings("rawtypes")
    public void unregister(@NonNull Object tag) {
        List<Subject> subjects = subjectMapper.get(tag);
        if (null != subjects) {
            subjectMapper.remove(tag);
        }
    }

    /**
     * 取消监听
     *
     * @param tagName
     * @param observable
     * @return
     */
    @SuppressWarnings("rawtypes")
    public RxBus unregister(@NonNull Object tagName,
                            @NonNull Observable<?> observable) {
        if (null == observable)
            return getInstance();
        List<Subject> subjects = subjectMapper.get(tagName);
        if (null != subjects) {
            subjects.remove((Subject<?, ?>) observable);
            if (isEmpty(subjects)) {
                subjectMapper.remove(tagName);
                LogUtils.logd("unregister" + tagName + "  size:" + subjects.size());
            }
        }
        return getInstance();
    }

    public void post(@NonNull Object content) {
        post(content.getClass().getName(), content);
    }

    /**
     * 触发事件
     *
     * @param content
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void post(@NonNull Object tag, @NonNull Object content) {
        LogUtils.logd("post" + "eventName: " + tag);
        List<Subject> subjectList = subjectMapper.get(tag);
        LogUtils.logd(subjectList.size() + "");
        if (!isEmpty(subjectList)) {
            for (Subject subject : subjectList) {
                subject.onNext(content);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection<Subject> collection) {
        return null == collection || collection.isEmpty();
    }

}
