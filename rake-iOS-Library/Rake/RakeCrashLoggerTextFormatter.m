//
//  RakeCrashLoggerTextFormatter.m
//  rake-iOS-Library
//
//  Created by 1002125 on 2016. 2. 3..
//
//
#import <CrashReporter/CrashReporter.h>

#import "RakeCrashLoggerTextFormatter.h"

@implementation RakeCrashLoggerTextFormatter

+ (NSString *) stringValueForCrashReport: (PLCrashReport *) report {
    return [PLCrashReportTextFormatter stringValueForCrashReport:report withTextFormat:PLCrashReportTextFormatiOS];
}

+ (NSString *)stringValueForStackTrace:(PLCrashReport *)report {
    NSMutableString* text = [NSMutableString string];
    
    
    /* If an exception stack trace is available, output a pseudo-thread to provide the frame info */
    if (report.exceptionInfo != nil && report.exceptionInfo.stackFrames != nil && [report.exceptionInfo.stackFrames count] > 0) {
        PLCrashReportExceptionInfo *exception = report.exceptionInfo;
        
        /* Write out the frames */
        NSUInteger numberBlankStackFrames = 0;
        
        for (NSUInteger frame_idx = 0; frame_idx < [exception.stackFrames count]; frame_idx++) {
            PLCrashReportStackFrameInfo *frameInfo = [exception.stackFrames objectAtIndex: frame_idx];
            NSString *formattedStackFrame = [self formatStackFrameOffset: frameInfo frameIndex: frame_idx - numberBlankStackFrames report: report];
            if (formattedStackFrame) {
                [text appendString: formattedStackFrame];
            } else {
                numberBlankStackFrames++;
            }
        }
        
        return text;
    }
    
    /* Threads */
    // PLCrashReportThreadInfo *crashed_thread = nil;
    NSInteger maxThreadNum = 0;
    for (PLCrashReportThreadInfo *thread in report.threads) {
        
        /* Write out the frames */
        NSUInteger numberBlankStackFrames = 0;
        
        for (NSUInteger frame_idx = 0; frame_idx < [thread.stackFrames count]; frame_idx++) {
            PLCrashReportStackFrameInfo *frameInfo = [thread.stackFrames objectAtIndex: frame_idx];
            NSString *formattedStackFrame = [self formatStackFrameOffset: frameInfo frameIndex: frame_idx report: report];
            if (formattedStackFrame) {
                //                if (frame_idx - numberBlankStackFrames == 0) {
                //                    /* Create the thread header. */
                //                    if (thread.crashed) {
                //                        [text appendFormat: @"Thread %ld Crashed:\n", (long) thread.threadNumber];
                //                        crashed_thread = thread;
                //                    } else {
                //                        [text appendFormat: @"Thread %ld:\n", (long) thread.threadNumber];
                //                    }
                //                }
                if (thread.crashed) {
                    [text appendString: formattedStackFrame];
                }
            } else {
                numberBlankStackFrames++;
            }
        }
        //        [text appendString: @"\n"];
        
        /* Track the highest thread number */
        maxThreadNum = MAX(maxThreadNum, thread.threadNumber);
    }
    
    return text;
}

//@end


//@implementation CrashLoggerTextFormatter (PrivateAPI)


/**
 * Format a stack frame for display in a thread backtrace.
 *
 * @param frameInfo The stack frame to format
 * @param frameIndex The frame's index
 * @param report The report from which this frame was acquired.
 *
 * @return Returns a formatted frame line.
 */
+ (NSString *)formatStackFrame: (PLCrashReportStackFrameInfo *) frameInfo
                    frameIndex: (NSUInteger) frameIndex
                        report: (PLCrashReport *) report
{
    /* Base image address containing instrumention pointer, offset of the IP from that base
     * address, and the associated image name */
    uint64_t baseAddress = 0x0;
    uint64_t pcOffset = 0x0;
    NSString *imageName = @"\?\?\?";
    
    PLCrashReportBinaryImageInfo *imageInfo = [report imageForAddress: frameInfo.instructionPointer];
    if (imageInfo != nil) {
        imageName = [imageInfo.imageName lastPathComponent];
        baseAddress = imageInfo.imageBaseAddress;
        pcOffset = frameInfo.instructionPointer - imageInfo.imageBaseAddress;
    }
    
    /* The frame has nothing useful, so return nil so it can be filtered out */
    if (frameInfo.instructionPointer == 0 && baseAddress == 0 && pcOffset == 0) {
        return nil;
    }
    
    /* Make sure UTF8/16 characters are handled correctly */
    NSInteger offset = 0;
    NSUInteger index = 0;
    for (index = 0; index < [imageName length]; index++) {
        NSRange range = [imageName rangeOfComposedCharacterSequenceAtIndex:index];
        if (range.length > 1) {
            offset += range.length - 1;
            index += range.length - 1;
        }
        if (index > 32) {
            imageName = [NSString stringWithFormat:@"%@...", [imageName substringToIndex:index - 1]];
            index += 3;
            break;
        }
    }
    if (index-offset < 36) {
        imageName = [imageName stringByPaddingToLength:36+offset withString:@" " startingAtIndex:0];
    }
    
    return [NSString stringWithFormat: @"%-4ld%@0x%08" PRIx64 " 0x%" PRIx64 " + %" PRId64 "\n",
            (long) frameIndex,
            imageName,
            frameInfo.instructionPointer,
            baseAddress,
            pcOffset];
}


/**
 * Format a stack frame for display in a thread backtrace.
 *
 * @param frameInfo The stack frame to format
 * @param frameIndex The frame's index
 * @param report The report from which this frame was acquired.
 *
 * @return Returns a formatted frame line.
 */
+ (NSString *)formatStackFrameOffset: (PLCrashReportStackFrameInfo *) frameInfo
                          frameIndex: (NSUInteger) frameIndex
                              report: (PLCrashReport *) report
{
    /* Base image address containing instrumention pointer, offset of the IP from that base
     * address, and the associated image name */
    uint64_t baseAddress = 0x0;
    uint64_t pcOffset = 0x0;
    NSString *imageName = @"\?\?\?";
    
    PLCrashReportBinaryImageInfo *imageInfo = [report imageForAddress: frameInfo.instructionPointer];
    if (imageInfo != nil) {
        imageName = [imageInfo.imageName lastPathComponent];
        baseAddress = imageInfo.imageBaseAddress;
        pcOffset = frameInfo.instructionPointer - imageInfo.imageBaseAddress;
    }
    
    /* The frame has nothing useful, so return nil so it can be filtered out */
    if (frameInfo.instructionPointer == 0 && baseAddress == 0 && pcOffset == 0) {
        return nil;
    }
    
    /* Make sure UTF8/16 characters are handled correctly */
    NSInteger offset = 0;
    NSUInteger index = 0;
    for (index = 0; index < [imageName length]; index++) {
        NSRange range = [imageName rangeOfComposedCharacterSequenceAtIndex:index];
        if (range.length > 1) {
            offset += range.length - 1;
            index += range.length - 1;
        }
        if (index > 32) {
            imageName = [NSString stringWithFormat:@"%@...", [imageName substringToIndex:index - 1]];
            index += 3;
            break;
        }
    }
    if (index-offset < 36) {
        imageName = [imageName stringByPaddingToLength:36+offset withString:@" " startingAtIndex:0];
    }
    
    return [NSString stringWithFormat: @"%@ %" PRId64 "\n",
            imageName,
            pcOffset];
}




@end
