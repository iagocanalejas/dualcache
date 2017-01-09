package com.iagocanalejas.retrofit;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.iagocanalejas.dualcache.interfaces.Cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * Created by Canalejas on 09/01/2017.
 */

public class CachedCallFactory extends CallAdapter.Factory {
    private final Cache<String, byte[]> mCachingSystem;
    private final Executor mAsyncExecutor;

    public CachedCallFactory(Context context, int appVersion) {
        this.mCachingSystem = BaseCache.getInstance(context, appVersion);
        this.mAsyncExecutor = new AndroidExecutor();
    }

    public CachedCallFactory(Cache<String, byte[]> cachingSystem) {
        this.mCachingSystem = cachingSystem;
        this.mAsyncExecutor = new AndroidExecutor();
    }

    public CachedCallFactory(Cache<String, byte[]> cachingSystem, Executor executor) {
        this.mCachingSystem = cachingSystem;
        this.mAsyncExecutor = executor;
    }

    @Override
    public CallAdapter<CachedCall<?>> get(final Type returnType, final Annotation[] annotations,
                                          final Retrofit retrofit) {

        TypeToken<?> token = TypeToken.get(returnType);
        if (token.getRawType() != CachedCall.class) {
            return null;
        }

        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException(
                    "SmartCall must have generic type (e.g., SmartCall<ResponseBody>)");
        }

        final Type responseType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        final Executor callbackExecutor = mAsyncExecutor;

        return new CallAdapter<CachedCall<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public <R> CachedCall<R> adapt(Call<R> call) {
                return new CachedCallImpl<>(callbackExecutor, call, responseType(), annotations,
                        retrofit, mCachingSystem);
            }
        };
    }

}
