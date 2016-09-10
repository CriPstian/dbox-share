$(document).ready(function() {
    var linksToSend = [];
    var options = {
        success: function(files) {
            if(files.length == 0) return;
            var photos = $("#photos");
            photos.empty();
            var textDiv = $("<div></div>");
            textDiv.addClass("text-center");
            textDiv.append("<span>Selected Photos:</span>");
            photos.append(textDiv);
            var container = $("<div></div>");
            container.addClass('row');
            var rowDiv = $("<div></div>");
            rowDiv.addClass("col-lg-10 col-lg-push-1");
            var row;

            $(files).each(function(index) {
                console.log(index);
                if(index % 6 == 0) {
                    row = $("<div></div>");
                    row.addClass("row");
                }
                var photo = this;
                linksToSend.push(photo.link);
                var photoDiv = $("<div></div>");
                photoDiv.addClass("col-lg-2");
                photoDiv.css("padding-right", "1");
                photoDiv.css("padding-left", "1");
                var img = $("<img/>");
                img.addClass("img-thumbnail");
                img.attr("src", photo.thumbnailLink.replace("bounding_box=75", "bounding_box=256"));
                img.appendTo(photoDiv);
                photoDiv.appendTo(row);
                if(index == files.length - 1 || index % 6 == 5) {
                    row.appendTo(rowDiv);
                }
            });
            rowDiv.appendTo(container);
            photos.append(container);

            var sendLinksButton = $("<div></div>");
            sendLinksButton.addClass("btn btn-default");
            sendLinksButton.on("click", sendLinks);
            sendLinksButton.text("DO IT!");
            photos.append(sendLinksButton);
        },
        linkType: "direct",
        multiselect: true,
        extensions:['.jpg','.jpeg']
    };
    var button = Dropbox.createChooseButton(options);
    $('#container').append(button);

    var sendLinks = function() {
        console.log("Sending links...");
        $.post("/links", { "links[]" : linksToSend }, function(response) {
            alert("Finished with sucess!");
        })
        .fail(function(error) {
            alert("Some error occured!!!: " + error);
        })
    }
});