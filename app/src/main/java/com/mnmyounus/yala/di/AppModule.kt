package com.mnmyounus.yala.di

import android.content.Context
import androidx.room.Room
import com.mnmyounus.yala.data.local.db.AppDatabase
import com.mnmyounus.yala.data.local.db.IntruderCaptureDao
import com.mnmyounus.yala.data.local.db.LockedAppDao
import com.mnmyounus.yala.data.repository.AppRepositoryImpl
import com.mnmyounus.yala.data.repository.IntruderRepositoryImpl
import com.mnmyounus.yala.data.repository.LockRepositoryImpl
import com.mnmyounus.yala.domain.repository.AppRepository
import com.mnmyounus.yala.domain.repository.IntruderRepository
import com.mnmyounus.yala.domain.repository.LockRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindLockRepository(impl: LockRepositoryImpl): LockRepository
    @Binds @Singleton abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository
    @Binds @Singleton abstract fun bindIntruderRepository(impl: IntruderRepositoryImpl): IntruderRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "yala.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideLockedAppDao(db: AppDatabase): LockedAppDao = db.lockedAppDao()
    @Provides fun provideIntruderCaptureDao(db: AppDatabase): IntruderCaptureDao = db.intruderCaptureDao()
}
