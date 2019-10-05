#include <jni.h>
#include <string>

#include <opencv2/opencv.hpp>

using namespace cv;
using namespace std;

extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_michael_cv_1app3_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    Mat src(Scalar(0,255,0));
    imshow("src",src);
    return env->NewStringUTF(hello.c_str());
}
