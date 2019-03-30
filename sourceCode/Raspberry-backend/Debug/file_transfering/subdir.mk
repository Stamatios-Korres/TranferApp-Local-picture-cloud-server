################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../file_transfering/FileTransfering.cpp 

OBJS += \
./file_transfering/FileTransfering.o 

CPP_DEPS += \
./file_transfering/FileTransfering.d 


# Each subdirectory must supply rules for building sources it contributes
file_transfering/%.o: ../file_transfering/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: Cross G++ Compiler'
	g++ -D__GXX_EXPERIMENTAL_CXX0X__ -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


