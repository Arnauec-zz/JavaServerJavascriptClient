$(document).ready(function() {
        var options = {
            valueNames: [ 'name' ],
            item: '<li><p class="name"></p></li>'
        };

        var values = [
        ];

        var llista = new List('llista', options, values);

        // Adds the message to the main div
        function log(text) {
            var xat = $("#xat").html();
            xat = text + "<br>" + xat;
            $("#xat").html(xat);
        }

        if (!window.WebSocket) {
            alert("FATAL ERROR: WebSockets are not supported.");
        }
        var ws;

        $('#nomUsuari').on("submit", function(event) {
            event.stopPropagation();
            ws = new WebSocket("ws://localhost:50000"); // Initialize of the WebSocket

            // Some dynamism to the page
            $("#xat").css("display", "inline");
            $("#llista").css("display", "inline");
            $("#nomUsuari").css("display", "none");
            $("#connect").css("display", "none");
            $("#enviar").css("display", "inline");
            $("#txtBox").css("display", "inline");
            $("#benvinguda").css("display", "none");
            $("#instruccions").css("display", "inline");

            // Actions performed whenever a connection is opened
            ws.onopen = function() {
                var nomUsuari = $("#nomUsuariBox").val();
                ws.send("/newuser " +  nomUsuari);
                log("Welcome to the room");

            }

            // Actions performed every time a message arrives
            ws.onmessage = function(event) {
                var str = event.data;
                if(str.includes("/newuser ")){
                    var talls = str.split(" ");
                    llista.add({ name : talls[1] });
                    llista.sort('name', { order: "asc" });

                } else if(str.includes("/deleteuser ")) {
                    var talls = str.split(" ");
                    llista.remove("name", talls[1]);
                    llista.sort('name', { order: "asc" });

                }else{
                    log(str);
                }
               }

            // Actions performed when the connection is closed
            ws.onclose = function() {
                log("You have left the room");
                ws = null;
            };
            return false;
            });

            $("#enviarText").on("submit", function() {
                var nomUsuari = $("#nomUsuariBox").val();
                if (ws) {
                    ws.send(nomUsuari + ": " + $('#txtBox').val());
                    $('#txtBox').val("");
                    $('#txtBox').focus();

                }
                return false
            });
            
        });