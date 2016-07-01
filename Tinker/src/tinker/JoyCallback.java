/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tinker;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author duemchen
 */
public interface JoyCallback {

    void setMotion(MqttMessage message);

}
