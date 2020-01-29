/* DO NOT EDIT THIS FILE - it is machine generated */
#include "dsutil.h"

/* Header for class com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl */

#ifndef _Included_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
#define _Included_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_close
  (JNIEnv *, jobject);

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    connect
 * Signature: (Ljava/net/InetAddress;I)V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_connect
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    create
 * Signature: (Ljava/net/InetAddress;I)V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_create
  (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_bind
  (JNIEnv *, jobject, jint, jobject);
/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_init
  (JNIEnv *, jclass);

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    send
 * Signature: (Ljava/net/DatagramPacket;)V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_send
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    receive
 * Signature: (Ljava/net/DatagramPacket;)V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_receive
  (JNIEnv *, jobject, jobject);

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    setReceiveBufferSize
 * Signature: (I)
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_setReceiveBufferSize
  (JNIEnv *, jobject, jint);
/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    getReceiveBufferSize
 * Signature: ()
 */
JNIEXPORT int JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_getReceiveBufferSize
  (JNIEnv *, jobject);
/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    setSendBufferSize
 * Signature: (I)
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_setSendBufferSize
  (JNIEnv *, jobject, jint);
/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    getSendBufferSize
 * Signature: ()
 */
JNIEXPORT int JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_getSendBufferSize
  (JNIEnv *, jobject);
#ifdef __cplusplus
}
#endif
#endif
