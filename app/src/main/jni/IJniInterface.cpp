#include <jni.h>
#include <assert.h>
#include <string.h>

/*
 * Method:    initJNIEnv
 */
jboolean Jni_iJNIE(JNIEnv *pEnv, jclass msgFactoryClass) {
    return JNI_TRUE;
}

/*
 * 每添加一位密码调用该方法，使用jni方法加密保存当前输入的这一位密码
 * Method:    appendPwd
 * Signature: (Ljava/lang/String;)V
 */
void Jni_aaP(JNIEnv *pEnv, jclass msgFactoryClass, jstring password) {
}

/*
 * 每删除一位密码调用该方法，从本地删除密码中删除
 * Method:    deleteOnePwd
 * Signature: ()V
 */
void Jni_adOP(JNIEnv *pEnv, jclass msgFactoryClass) {

}

/*
 * 清空密码
 * Method:    clearPwd
 * Signature: ()V
 */
void Jni_acP(JNIEnv *pEnv, jclass msgFactoryClass) {

}

/*
 * 获取完整的加密密码
 * Method:    getEncryptedPin
 * Signature: ()Ljava/lang/String;
 */
jstring Jni_gEP(JNIEnv *pEnv, jclass msgFactoryClass) {
	jstring jret = NULL;
    jret = pEnv->NewStringUTF("111111");
    return jret;
}

/**
 * 方法对应表
 */
static JNINativeMethod gMethods[] =
		{
			{ "iJNIE", "()Z", (void*) Jni_iJNIE },
			{ "aP", "(Ljava/lang/String;)V", (void*) Jni_aaP },
			{ "dOP", "()V", (void*) Jni_adOP },
			{ "cP", "()V", (void*) Jni_acP },
			{ "gEP", "(Ljava/lang/String;)Ljava/lang/String;", (void*) Jni_gEP },
		};

/*
 * 为某一个类注册本地方法
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
		JNINativeMethod* gMethods, int numMethods) {
	jclass clazz;
	clazz = env->FindClass(className);
	if (clazz == NULL) {
		return JNI_FALSE;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

/*
 * 为所有类注册本地方法
 */
static int registerNatives(JNIEnv* env) {
	const char* kClassName = "com/vigorous/pwpopwindow/jni/IJniInterface"; //指定要注册的类
	return registerNativeMethods(env, kClassName, gMethods,
			sizeof(gMethods) / sizeof(gMethods[0]));
}

/*
 * System.loadLibrary("lib")时调用
 * 如果成功返回JNI版本, 失败返回-1
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		return -1;
	}
	assert(env != NULL);

	if (!registerNatives(env)) {
		return -1;
	}
	result = JNI_VERSION_1_4;

	return result;
}
